package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 상품 리포지토리
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리로 찾기 → select * from products where category = ?
    List<Product> findByCategory(String category);

    // 상품명에 검색어가 포함된 상품 찾기 → select * from products where name like %?%
    List<Product> findByNameContaining(String keyword);
}
