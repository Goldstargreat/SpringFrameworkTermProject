package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.MemberGrade;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 모든 화면에 "로그인한 손님의 등급"을 넣어 주는 클래스
// @ControllerAdvice + @ModelAttribute : 모든 컨트롤러가 화면을 그리기 전에 이 메서드가 먼저 실행된다
// 화면 상단의 "홍길동님 (골드)", 상품 상세의 "골드 회원가" 에 쓴다 (화면에서 ${loginMemberGrade})
// 등급은 주문이 배송완료될 때 바뀌므로, 세션에 넣어 두지 않고 매번 DB 에서 읽는다
@ControllerAdvice
public class LoginGradeAdvice {

    private final MemberService memberService;

    public LoginGradeAdvice(MemberService memberService) {
        this.memberService = memberService;
    }

    // 로그인한 일반 회원이면 그 회원의 등급, 로그인하지 않았거나 관리자면 null
    @ModelAttribute("loginMemberGrade")
    public MemberGrade loginMemberGrade(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        if (memberId == null || "ADMIN".equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE))) {
            return null;
        }
        Member member = memberService.findById(memberId);
        return member == null ? null : member.getGrade();
    }
}
