package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.CartItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import kr.ac.kopo.gnuyog.goldstarmall.validator.ProductValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 상품 서비스 : 상품 조회, (관리자) 상품 등록, 가격/재고 수정, 삭제
@Service
@Transactional(readOnly = true)
public class ProductService {

    // 카테고리 목록 : 메인 화면 메뉴와 관리자 상품 등록 화면에서 함께 쓴다
    private static final List<String> CATEGORIES = List.of(
            "패션의류", "잡화", "뷰티", "식품", "주방용품",
            "생활용품", "홈인테리어", "가전디지털", "스포츠/레저", "자동차용품",
            "음반/dvd", "완구/취미", "문구/오피스", "반려동물용품", "헬스/건강식품");

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                          CartItemRepository cartItemRepository,
                          OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // 카테고리 목록
    public List<String> getCategories() {
        return CATEGORIES;
    }

    // 상품 목록 조회
    // - 검색어가 있으면 이름으로 검색
    // - 카테고리가 있으면 카테고리로 조회
    // - 둘 다 없으면 전체 조회
    public List<Product> findProducts(String category, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productRepository.findByNameContaining(keyword.trim());
        }
        if (category != null && !category.trim().isEmpty()) {
            return productRepository.findByCategory(category);
        }
        return productRepository.findAll();
    }

    // 상품 1개 조회 (없으면 null)
    public Product findById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    // (관리자) 상품 등록
    // 클래스에 readOnly = true 가 붙어 있으므로, 저장/수정하는 메서드에는 @Transactional 을 따로 붙인다
    @Transactional
    public Product create(String name, String category, int price, int stock, String description, String icon) {
        ProductValidator.validateNewProduct(name, category, CATEGORIES, price, stock, description, icon);

        Product product = new Product();
        product.setName(name.trim());
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        // 이모지를 비워 두면 기본 상자 이모지를 쓴다
        if (icon == null || icon.trim().isEmpty()) {
            product.setIcon("📦");
        } else {
            product.setIcon(icon.trim());
        }
        return productRepository.save(product);
    }

    // (관리자) 가격, 재고 수정
    @Transactional
    public void updatePriceAndStock(Long productId, int price, int stock) {
        Product product = findById(productId);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        ProductValidator.validatePriceAndStock(price, stock);
        // 변경 감지로 update SQL 이 실행된다
        product.setPrice(price);
        product.setStock(stock);
    }

    // (관리자) 상품 삭제
    // 주문된 적 있는 상품은 주문 내역에 남아 있어야 하므로 삭제할 수 없다 (대신 재고를 0으로 하면 품절로 판매 중지)
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findById(productId);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        if (orderItemRepository.existsByProduct(product)) {
            throw new BusinessException("'" + product.getName() + "'은(는) 주문 내역이 있어 삭제할 수 없습니다. 재고를 0으로 바꿔 판매를 중지하세요.");
        }
        // 이 상품을 장바구니에 담아 둔 회원이 있으면 그 항목부터 지운다 (외래키 때문에 순서가 중요하다)
        cartItemRepository.deleteAll(cartItemRepository.findByProduct(product));
        productRepository.delete(product);
    }
}
