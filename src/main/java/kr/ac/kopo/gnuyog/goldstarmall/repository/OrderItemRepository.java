package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

// 주문 상품 리포지토리
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 이 상품이 한 번이라도 주문된 적이 있는지 → select ... from order_items where product_id = ?
    boolean existsByProduct(Product product);
}
