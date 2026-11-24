package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.MemberGrade;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// 회원 등급 서비스 : 누적 구매액 계산, 등급 자동 책정
// 규칙
//  - 누적 구매액 = 배송완료된 주문의 결제 금액 합계 (취소/거절, 아직 배송 중인 주문은 세지 않는다)
//  - 누적 구매액이 기준 금액(MemberGrade)을 충족하면 그 등급이 자동으로 정해진다
//    (주문이 배송완료될 때, 서버를 켤 때 다시 계산한다)
//  - 등급은 기준 금액으로만 정해지고, 관리자가 직접 바꾸지 않는다
//  - 관리자 계정은 주문하지 않으므로 등급을 다루지 않는다
@Service
@Transactional
public class GradeService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    public GradeService(MemberRepository memberRepository, OrderRepository orderRepository) {
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
    }

    // 한 회원의 누적 구매액 (배송완료 주문의 결제 금액 합계)
    @Transactional(readOnly = true)
    public long totalPurchase(Member member) {
        return orderRepository.sumTotalPriceByMemberAndStatus(member, ShippingStatus.DELIVERED);
    }

    // 다음 등급까지 남은 금액 (가장 높은 등급이면 0)
    @Transactional(readOnly = true)
    public long remainingToNextGrade(Member member) {
        MemberGrade next = member.getGrade().getNext();
        if (next == null) {
            return 0;
        }
        return Math.max(0, next.getMinAmount() - totalPurchase(member));
    }

    // 한 회원의 등급을 누적 구매액에 맞게 다시 정한다 (관리자 계정은 그대로)
    // 주문이 배송완료될 때 OrderService 가 부른다
    public void refreshGrade(Member member) {
        if ("ADMIN".equals(member.getRole())) {
            return;
        }
        member.setGrade(MemberGrade.fromAmount(totalPurchase(member)));
    }

    // 모든 회원의 등급을 다시 정한다 (서버를 켤 때 DataInitializer 가 부른다)
    // 시연용 SQL 로 넣은 주문처럼 앱을 거치지 않고 배송완료된 주문도 반영하기 위해서다
    public void refreshAllGrades() {
        for (Member member : memberRepository.findAll()) {
            refreshGrade(member);
        }
    }


    // (관리자) 회원 번호 → 누적 구매액 (회원관리 목록에 보여 주기 위해)
    @Transactional(readOnly = true)
    public Map<Long, Long> totalPurchaseByMember() {
        Map<Long, Long> result = new HashMap<>();
        for (Member member : memberRepository.findAll()) {
            result.put(member.getId(), totalPurchase(member));
        }
        return result;
    }
}
