package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 상품 서비스 : 상품 조회, (관리자) 상품 등록, 가격/재고 수정
@Service
@Transactional(readOnly = true)
public class ProductService {

    // 카테고리 목록 : 메인 화면 메뉴와 관리자 상품 등록 화면에서 함께 쓴다
    private static final List<String> CATEGORIES = List.of(
            "패션의류", "잡화", "뷰티", "식품", "주방용품",
            "생활용품", "홈인테리어", "가전디지털", "스포츠/레저", "자동차용품",
            "음반/dvd", "완구/취미", "문구/오피스", "반려동물용품", "헬스/건강식품");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        if (name.trim().isEmpty()) {
            throw new IllegalStateException("상품명을 입력해 주세요.");
        }
        if (!CATEGORIES.contains(category)) {
            throw new IllegalStateException("카테고리를 선택해 주세요.");
        }
        checkPriceAndStock(price, stock);
        // DB 컬럼 길이보다 길면 저장할 때 오류가 나므로 미리 막는다 (Product 의 @Column length 참고)
        if (name.trim().length() > 100) {
            throw new IllegalStateException("상품명은 100글자까지 입력할 수 있습니다.");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalStateException("상품 설명은 500글자까지 입력할 수 있습니다.");
        }
        if (icon != null && icon.trim().length() > 10) {
            throw new IllegalStateException("이모지는 한두 개만 입력해 주세요.");
        }

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
            throw new IllegalStateException("상품을 찾을 수 없습니다.");
        }
        checkPriceAndStock(price, stock);
        // 변경 감지로 update SQL 이 실행된다
        product.setPrice(price);
        product.setStock(stock);
    }

    // 가격은 1원 이상, 재고는 0개 이상이어야 한다
    private void checkPriceAndStock(int price, int stock) {
        if (price < 1) {
            throw new IllegalStateException("가격은 1원 이상이어야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalStateException("재고는 0개 이상이어야 합니다.");
        }
    }
}
