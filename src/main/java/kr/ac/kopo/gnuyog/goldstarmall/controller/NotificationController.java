package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 회원 알림 화면 : 내 문의에 달린 답변, 내 상품평에 달린 관리자 코멘트
// SecurityConfig 에서 로그인한 손님만 들어올 수 있게 막아 두었다 (관리자는 알림을 받지 않는다)
@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 알림 목록 : 이 화면을 열면 알림이 모두 "확인함"이 되어 상단 숫자가 사라진다
    @GetMapping("/notifications")
    public String list(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        model.addAttribute("notifications", notificationService.readAll(memberId));
        // 상단 숫자는 MemberNoticeAdvice 가 이 메서드보다 먼저 계산하므로, 방금 모두 읽었으니 0 으로 덮어쓴다
        model.addAttribute("myNoticeCount", 0L);
        return "member/notifications";
    }
}
