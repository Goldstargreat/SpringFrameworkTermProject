package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 주문 리포지토리
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 한 회원의 주문 목록을 최신순으로 → select * from orders where member_id = ? order by id desc
    List<Order> findByMemberOrderByIdDesc(Member member);

    // 회원 화면용 내 주문 목록 : 회원이 삭제(숨김)한 주문은 빼고 최신순으로
    @Query("select o from Order o where o.member = :member"
            + " and (o.hiddenByMember is null or o.hiddenByMember = false) order by o.id desc")
    List<Order> findVisibleByMember(@Param("member") Member member);

    // 전체 주문 목록을 최신순으로 (관리자 화면용)
    List<Order> findAllByOrderByIdDesc();

    // 주문번호로 주문 찾기 → select * from orders where order_number = ? (주문번호 중복 검사, 없으면 null)
    Order findByOrderNumber(String orderNumber);

    // 아직 주문번호가 없는 주문들 (SQL 로 직접 넣은 시연용 주문 등) → 서버를 켤 때 기본 주문번호를 채운다
    List<Order> findByOrderNumberIsNull();

    // 한 회원의 누적 구매액 : status(배송완료) 주문의 결제 금액 합계 (주문이 없으면 0)
    // → select coalesce(sum(total_price), 0) from orders where member_id = ? and status = 'DELIVERED'
    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.member = :member and o.status = :status")
    long sumTotalPriceByMemberAndStatus(@Param("member") Member member, @Param("status") ShippingStatus status);
}
