package kr.ac.kopo.gnuyog.goldstarmall.repository;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 주문 리포지토리
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 한 회원의 주문 목록을 최신순으로 → select * from orders where member_id = ? order by id desc
    List<Order> findByMemberOrderByIdDesc(Member member);

    // 전체 주문 목록을 최신순으로 (관리자 화면용)
    List<Order> findAllByOrderByIdDesc();
}
