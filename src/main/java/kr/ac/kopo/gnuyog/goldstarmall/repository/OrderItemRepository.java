package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.OrderItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    // (관리자) 상품별 누적 판매량, 누적 매출 : 취소된 주문(status)은 빼고 상품마다 더한다
    // 한 줄 = [상품 번호, 판매 수량 합계, 매출 합계(주문 당시 가격 x 수량)]
    // → select product_id, sum(quantity), sum(order_price * quantity) from order_items ... group by product_id
    @Query("select oi.product.id, sum(oi.quantity), sum(oi.orderPrice * oi.quantity) from OrderItem oi " +
           "where oi.order.status <> :excluded group by oi.product.id")
    List<Object[]> sumSalesByProduct(@Param("excluded") ShippingStatus excluded);
}
