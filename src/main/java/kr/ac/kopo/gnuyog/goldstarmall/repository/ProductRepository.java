package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// 상품 리포지토리
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리로 찾기 → select * from products where category = ?
    List<Product> findByCategory(String category);

    // 상품명에 검색어가 포함된 상품 찾기 → select * from products where name like %?%
    List<Product> findByNameContaining(String keyword);

    // ===== 페이지 나누기용 (메인 화면) =====
    // Pageable 을 넘기면 "몇 번째 페이지, 몇 개씩"만 잘라서 가져온다 (SQL 의 limit ... offset ...)
    // Page 에는 상품 목록과 함께 전체 개수, 전체 페이지 수가 들어 있다
    // 전체 상품은 JpaRepository 가 기본으로 주는 findAll(Pageable) 을 쓴다
    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContaining(String keyword, Pageable pageable);

    // ===== 홈 화면 =====
    // 신상품 : 기준 시각 이후에 등록한 상품 중 최근 등록 순 8개
    // → select * from products where created_at >= ? order by created_at desc, id desc limit 8
    List<Product> findTop8ByCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(LocalDateTime since);
}
