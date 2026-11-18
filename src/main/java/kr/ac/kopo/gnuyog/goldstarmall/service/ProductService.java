package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 상품 서비스 : 상품 조회
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}
