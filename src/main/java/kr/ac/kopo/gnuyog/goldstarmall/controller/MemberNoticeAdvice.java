package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 로그인한 손님에게 "아직 확인하지 않은 알림 개수"를 모든 화면에 넣어 주는 클래스
// 화면 상단의 "알림 2" 처럼 빨간 숫자로 보여 준다 (화면에서 ${myNoticeCount})
// 관리자가 내 문의에 답변하거나 내 상품평에 코멘트를 달면 숫자가 올라간다
@ControllerAdvice
public class MemberNoticeAdvice {

    private final NotificationService notificationService;

    public MemberNoticeAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 확인하지 않은 알림 개수 (로그인하지 않았거나 관리자면 null)
    @ModelAttribute("myNoticeCount")
    public Long myNoticeCount(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        if (memberId == null || "ADMIN".equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE))) {
            return null;
        }
        return notificationService.countUnread(memberId);
    }
}
