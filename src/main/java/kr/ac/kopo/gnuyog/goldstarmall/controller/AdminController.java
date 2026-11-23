package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 관리자 컨트롤러 : 배송관리(배송 상태 변경, 주문 삭제), 회원관리(회원 삭제)
// AdminCheckInterceptor 때문에 관리자(ADMIN)만 들어올 수 있다
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final MemberService memberService;

    public AdminController(OrderService orderService, MemberService memberService) {
        this.orderService = orderService;
        this.memberService = memberService;
    }

    // 전체 주문 목록
    @GetMapping("/orders")
    public String orders(Model model) {
        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    // 배송 상태를 다음 단계로 변경
    @PostMapping("/orders/{orderId}/next")
    public String next(@PathVariable("orderId") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.moveToNextStatus(orderId);
            redirectAttributes.addFlashAttribute("message", orderId + "번 주문의 배송 상태를 변경했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // 주문 삭제 (배송완료, 주문취소된 주문만)
    @PostMapping("/orders/{orderId}/delete")
    public String deleteOrder(@PathVariable("orderId") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(orderId);
            redirectAttributes.addFlashAttribute("message", orderId + "번 주문을 삭제했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // 전체 회원 목록
    @GetMapping("/members")
    public String members(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "admin/members";
    }

    // 회원 삭제 (회원의 장바구니, 주문 내역도 함께 삭제된다)
    @PostMapping("/members/{memberId}/delete")
    public String deleteMember(@PathVariable("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        try {
            memberService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("message", memberId + "번 회원을 삭제했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
