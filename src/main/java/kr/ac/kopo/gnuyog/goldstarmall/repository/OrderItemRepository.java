package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 주문 상품 리포지토리
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 이 상품이 한 번이라도 주문된 적이 있는지 → select ... from order_items where product_id = ?
    boolean existsByProduct(Product product);

    // 이 회원이 이 상품을 주문했고, 그 주문이 status 상태인지 (상품평 쓰기 자격 확인 : 배송완료)
    @Query("select count(oi) > 0 from OrderItem oi " +
           "where oi.order.member = :member and oi.product = :product and oi.order.status = :status")
    boolean existsOrderedBy(@Param("member") Member member,
                            @Param("product") Product product,
                            @Param("status") ShippingStatus status);
}
