package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.DailyDeal;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ProductOption;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.InquiryRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ReviewRepository;
import kr.ac.kopo.gnuyog.goldstarmall.validator.ProductValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 상품 서비스 : 상품 조회, (관리자) 상품 등록 / 가격·재고 수정 / 로켓 배송 / 판매량 초기화 / 삭제
// 클래스에 readOnly = true 가 붙어 있으므로, 저장/수정하는 메서드에는 @Transactional 을 따로 붙인다
@Service
@Transactional(readOnly = true)
public class ProductService {

    // 카테고리 목록 : 메인 화면 메뉴와 관리자 상품 등록 화면에서 함께 쓴다
    private static final List<String> CATEGORIES = List.of(
            "패션의류", "잡화", "뷰티", "식품", "주방용품",
            "생활용품", "홈인테리어", "가전디지털", "스포츠/레저", "자동차용품",
            "음반/dvd", "완구/취미", "문구/오피스", "반려동물용품", "헬스/건강식품");

    // 홈 화면 섹션마다 보여 줄 상품 수 (한 줄에 4개씩 2줄)
    public static final int HOME_SECTION_SIZE = 8;

    // 신상품 기준 : 등록한 지 이 일수 이내인 상품
    public static final int NEW_PRODUCT_DAYS = 30;

    // 상품 목록 화면 한 페이지에 보여 줄 상품 수
    public static final int PAGE_SIZE = 12;

    // 이모지를 비워 두고 상품을 등록하면 쓰는 기본 이모지
    private static final String DEFAULT_ICON = "📦";

    // 판매로 치지 않는 주문 상태 : 주문취소, 주문거절, 반품완료 (환불된 주문)
    private static final List<ShippingStatus> VOIDED_STATUSES =
            List.of(ShippingStatus.CANCELED, ShippingStatus.REJECTED, ShippingStatus.RETURNED);

    // OrderItemRepository.sumSalesByProduct 결과 한 줄의 칸 순서 : [상품 번호, 판매 수량, 매출]
    private static final int SALES_PRODUCT_ID = 0;
    private static final int SALES_QUANTITY = 1;
    private static final int SALES_AMOUNT = 2;

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;

    public ProductService(ProductRepository productRepository,
                          CartItemRepository cartItemRepository,
                          OrderItemRepository orderItemRepository,
                          ReviewRepository reviewRepository,
                          InquiryRepository inquiryRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
        this.inquiryRepository = inquiryRepository;
    }

    // ===== 상품 조회 =====

    // 카테고리 목록
    public List<String> getCategories() {
        return CATEGORIES;
    }

    // 상품 1개 조회 (없으면 null)
    public Product findById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    // 상품 1개 조회 (없으면 "상품을 찾을 수 없습니다." 예외) : 관리자 수정/삭제에서 쓴다
    private Product getProduct(Long productId) {
        Product product = findById(productId);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        return product;
    }

    // 상품 목록 한 페이지 조회 (상품 목록 화면)
    // - 찾는 조건은 findProducts 와 같다 (검색어 > 카테고리 > 전체)
    // - page 는 1부터 시작 (화면에 보이는 번호). Spring Data 는 0부터 세므로 1을 뺀다
    // - 없는 페이지 번호(예: 전체 8쪽인데 99쪽)를 달라고 하면 마지막 페이지를 보여 준다
    public Page<Product> findProductPage(String category, String keyword, int page) {
        int pageIndex = Math.max(page, 1) - 1;
        Page<Product> result = findPage(category, keyword, pageIndex);

        int lastPage = result.getTotalPages();
        if (lastPage > 0 && page > lastPage) {
            result = findPage(category, keyword, lastPage - 1);
        }
        return result;
    }

    // 한 페이지 조회 : 상품 번호 순서로 PAGE_SIZE 개씩 (pageIndex 는 0부터)
    private Page<Product> findPage(String category, String keyword, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, Sort.by("id"));
        if (hasText(keyword)) {
            return productRepository.findByNameContaining(keyword.trim(), pageable);
        }
        if (hasText(category)) {
            return productRepository.findByCategory(category, pageable);
        }
        return productRepository.findAll(pageable);
    }

    // 상품 목록 조회 (페이지 나누기 없이 전부 : 관리자 상품관리 화면)
    // 1) 검색어가 있으면 이름으로 검색  2) 카테고리가 있으면 카테고리로 조회  3) 둘 다 없으면 전체 조회
    public List<Product> findProducts(String category, String keyword) {
        if (hasText(keyword)) {
            return productRepository.findByNameContaining(keyword.trim());
        }
        if (hasText(category)) {
            return productRepository.findByCategory(category);
        }
        return productRepository.findAll();
    }

    // ===== 홈 화면 섹션 (섹션마다 최대 HOME_SECTION_SIZE 개) =====

    // 오늘의 할인 상품 : 오늘 할인하는 카테고리에서 재고가 있는 상품 (상품 번호 순)
    public List<Product> findTodayDealProducts() {
        String todayCategory = DailyDeal.today().getCategory();
        List<Product> inStock = new ArrayList<>();
        for (Product product : productRepository.findByCategory(todayCategory)) {
            if (product.getStock() > 0) {
                inStock.add(product);
            }
        }
        return firstItems(inStock, HOME_SECTION_SIZE);
    }

    // 인기 상품 : 누적 판매량이 많은 순서 (품절 상품은 빼고, 팔린 적 없는 상품은 들어가지 않는다)
    public List<Product> findPopularProducts() {
        Map<Long, Long> salesQuantity = salesQuantityByProduct();
        List<Product> inStock = new ArrayList<>();
        for (Product product : productRepository.findAllById(salesQuantity.keySet())) {
            if (product.getStock() > 0) {
                inStock.add(product);
            }
        }
        // 판매량이 많은 상품이 앞으로 오도록 정렬한다 (b 와 a 를 바꿔 비교하면 큰 순서가 된다)
        inStock.sort((a, b) -> Long.compare(salesQuantity.get(b.getId()), salesQuantity.get(a.getId())));
        return firstItems(inStock, HOME_SECTION_SIZE);
    }

    // 로켓 배송 상품 : 관리자가 로켓 배송을 켠 상품 중 재고가 있는 것 (무작위)
    public List<Product> findFastDeliveryProducts() {
        List<Product> fastDelivery = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (product.isShowFastDelivery()) {
                fastDelivery.add(product);
            }
        }
        // 홈 화면을 열 때마다 다른 상품이 보이도록 순서를 섞은 뒤 앞에서부터 보여 준다
        Collections.shuffle(fastDelivery);
        return firstItems(fastDelivery, HOME_SECTION_SIZE);
    }

    // 신상품 : 최근 NEW_PRODUCT_DAYS 일 안에 등록한 상품 (최근 등록 순, 최대 8개)
    public List<Product> findNewProducts() {
        LocalDateTime since = LocalDateTime.now().minusDays(NEW_PRODUCT_DAYS);
        return productRepository.findTop8ByCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(since);
    }

    // 품절 임박 상품 : 재고가 1 ~ 2개 남은 상품 (상품 번호 순)
    public List<Product> findLowStockProducts() {
        List<Product> lowStock = new ArrayList<>();
        for (Product product : productRepository.findAll(Sort.by("id"))) {
            if (product.isLowStock()) {
                lowStock.add(product);
            }
        }
        return firstItems(lowStock, HOME_SECTION_SIZE);
    }

    // 목록의 앞에서부터 최대 count 개 (목록이 더 짧으면 전부)
    private List<Product> firstItems(List<Product> products, int count) {
        if (products.size() <= count) {
            return products;
        }
        return new ArrayList<>(products.subList(0, count));
    }

    // ===== (관리자) 판매량 / 매출 =====

    // 상품별 누적 판매량 : 상품 번호 → 판매 수량 (취소/거절/반품된 주문은 빼고, 한 번도 안 팔린 상품은 없음)
    public Map<Long, Long> salesQuantityByProduct() {
        return sumSales(SALES_QUANTITY);
    }

    // 상품별 누적 매출 : 상품 번호 → 매출(원) (주문 당시 가격 기준이라 가격을 바꿔도 그대로다)
    public Map<Long, Long> salesAmountByProduct() {
        return sumSales(SALES_AMOUNT);
    }

    // 상품별 판매 합계에서 원하는 칸(판매 수량 또는 매출)만 꺼낸다
    private Map<Long, Long> sumSales(int column) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : orderItemRepository.sumSalesByProduct(VOIDED_STATUSES)) {
            Long productId = (Long) row[SALES_PRODUCT_ID];
            long value = ((Number) row[column]).longValue();    // sum 결과는 Long 이나 Integer 로 올 수 있다
            result.put(productId, value);
        }
        return result;
    }

    // 한 상품의 누적 판매량/매출 초기화 : 주문 기록은 그대로 두고, 지금부터 다시 센다
    @Transactional
    public Product resetSales(Long productId) {
        Product product = getProduct(productId);
        product.setSalesResetAt(LocalDateTime.now());
        return product;
    }

    // 모든 상품의 누적 판매량/매출 초기화
    @Transactional
    public void resetAllSales() {
        LocalDateTime now = LocalDateTime.now();
        for (Product product : productRepository.findAll()) {
            product.setSalesResetAt(now);
        }
    }

    // ===== (관리자) 상품 등록 / 수정 / 삭제 =====

    // 상품 등록
    @Transactional
    public Product create(String name, String category, int price, int stock, String description, String icon,
                          String manufacturer, String origin, boolean fastDelivery) {
        ProductValidator.validateNewProduct(name, category, CATEGORIES, price, stock, description, icon);
        ProductValidator.validateProductInfo(manufacturer, origin);

        Product product = new Product();
        product.setName(name.trim());
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        // 이모지를 비워 두면 기본 상자 이모지를 쓴다
        if (hasText(icon)) {
            product.setIcon(icon.trim());
        } else {
            product.setIcon(DEFAULT_ICON);
        }
        // 제조사/원산지는 선택 입력 : 비워 두면 null (상세 화면에는 "-" 로 보인다)
        product.setManufacturer(blankToNull(manufacturer));
        product.setOrigin(blankToNull(origin));
        product.setFastDelivery(fastDelivery);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // 가격, 재고 수정 (변경 감지로 update SQL 이 실행된다)
    @Transactional
    public void updatePriceAndStock(Long productId, int price, int stock) {
        Product product = getProduct(productId);
        ProductValidator.validatePriceAndStock(price, stock);
        // 옵션 상품의 재고는 옵션 재고의 합계라서 여기서 바꿀 수 없다 (가격만 바꿀 수 있다)
        if (product.isHasOptions() && stock != product.getStock()) {
            throw new BusinessException("'" + product.getName() + "'은(는) 옵션 상품이라 재고를 옵션별로 관리합니다. 가격만 바꿀 수 있습니다.");
        }
        product.setPrice(price);
        product.setStock(stock);
    }

    // 로켓 배송 켜기/끄기 : 지금 값의 반대로 바꾼다
    @Transactional
    public Product toggleFastDelivery(Long productId) {
        Product product = getProduct(productId);
        product.setFastDelivery(!product.isFastDelivery());
        return product;
    }

    // 상품 삭제
    // 주문된 적 있는 상품은 주문 내역에 남아 있어야 하므로 삭제할 수 없다 (대신 재고를 0으로 하면 품절로 판매 중지)
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getProduct(productId);
        if (orderItemRepository.existsByProduct(product)) {
            throw new BusinessException("'" + product.getName() + "'은(는) 주문 내역이 있어 삭제할 수 없습니다. 재고를 0으로 바꿔 판매를 중지하세요.");
        }
        // 이 상품을 가리키는 것들을 먼저 지우고 상품을 지운다 (외래키 때문에 순서가 중요하다)
        // - 장바구니 : 이 상품을 담아 둔 회원이 있을 수 있다
        // - 상품평 : 배송완료된 주문이 있어야 쓸 수 있어서 보통은 없다
        // - 상품문의 : 주문 없이도 달릴 수 있다
        cartItemRepository.deleteAll(cartItemRepository.findByProduct(product));
        reviewRepository.deleteAll(reviewRepository.findByProduct(product));
        inquiryRepository.deleteAll(inquiryRepository.findByProduct(product));
        productRepository.delete(product);
    }

    // ===== 손님 주문 =====

    // 손님이 고른 옵션 확인 : 옵션이 있는 상품이면 꼭 골라야 하고, 그 상품의 옵션이어야 한다
    // 옵션이 없는 상품이면 null 을 돌려준다 (넘어온 optionId 는 무시)
    public ProductOption resolveOption(Product product, Long optionId) {
        if (!product.isHasOptions()) {
            return null;
        }
        if (optionId == null) {
            throw new BusinessException("옵션을 선택해 주세요.");
        }
        for (ProductOption option : product.getOptions()) {
            if (option.getId().equals(optionId)) {
                return option;
            }
        }
        throw new BusinessException("선택한 옵션을 찾을 수 없습니다.");
    }

    // ===== 도우미 =====

    // 값이 있는지 (null 이거나 공백뿐이면 없는 것)
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // 빈 문자열은 null 로, 앞뒤 공백은 지워서 저장한다
    private String blankToNull(String value) {
        if (hasText(value)) {
            return value.trim();
        }
        return null;
    }
}
