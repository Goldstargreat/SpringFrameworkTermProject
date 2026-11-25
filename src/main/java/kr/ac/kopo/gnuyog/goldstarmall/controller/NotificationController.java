package kr.ac.kopo.gnuyog.goldstarmall.controller;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("notifications", notificationService.readAll(loginMemberId(session)));
        // 상단 숫자는 MemberNoticeAdvice 가 이 메서드보다 먼저 계산하므로, 방금 모두 읽었으니 0 으로 덮어쓴다
        model.addAttribute("myNoticeCount", 0L);
        return "member/notifications";
    }

    // 알림 1건 삭제 : kind 는 inquiry(문의 답변) 또는 review(상품평 코멘트), id 는 문의/상품평 번호
    // 확인한 알림만 지울 수 있고, 문의/상품평 자체는 지워지지 않는다
    @PostMapping("/notifications/{kind}/{id}/delete")
    public String delete(@PathVariable("kind") String kind,
                         @PathVariable("id") Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            notificationService.hide(loginMemberId(session), kind, id);
            redirectAttributes.addFlashAttribute("message", "알림을 삭제했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notifications";
    }

    // 확인한 알림 전체 삭제
    @PostMapping("/notifications/delete-all")
    public String deleteAll(HttpSession session, RedirectAttributes redirectAttributes) {
        int count = notificationService.hideAllRead(loginMemberId(session));
        if (count == 0) {
            redirectAttributes.addFlashAttribute("message", "삭제할 알림이 없습니다.");
        } else {
            redirectAttributes.addFlashAttribute("message", "확인한 알림 " + count + "건을 삭제했습니다.");
        }
        return "redirect:/notifications";
    }

    // 로그인한 회원 번호 (세션에 들어 있다)
    private Long loginMemberId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
    }
}
