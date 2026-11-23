package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 주문 컨트롤러 (로그인한 사람만 들어올 수 있다)
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 주문하기 : 장바구니 화면의 주문서에서 넘어온다
    @PostMapping
    public String create(@RequestParam("receiverName") String receiverName,
                         @RequestParam("receiverPhone") String receiverPhone,
                         @RequestParam("address") String address,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);

        // 받는 사람 정보가 비어 있으면 주문할 수 없다
        if (receiverName.trim().isEmpty() || receiverPhone.trim().isEmpty() || address.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "받는 사람, 연락처, 배송지를 모두 입력해 주세요.");
            return "redirect:/cart";
        }

        try {
            Order order = orderService.createOrder(memberId, receiverName.trim(), receiverPhone.trim(), address.trim());
            redirectAttributes.addFlashAttribute("message", "주문이 완료되었습니다.");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    // 내 주문 목록
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        List<Order> orders = orderService.findMyOrders(memberId);
        model.addAttribute("orders", orders);
        return "order/list";
    }

    // 주문 상세 + 배송 상태 추적
    @GetMapping("/{orderId}")
    public String detail(@PathVariable("orderId") Long orderId,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        try {
            Order order = orderService.findMyOrder(memberId, orderId);
            model.addAttribute("order", order);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }

        // 배송 진행 막대에 표시할 단계 이름 (순서대로 1~5단계)
        List<String> steps = List.of("주문접수", "입고", "출고", "배송중", "배송완료");
        model.addAttribute("steps", steps);
        return "order/detail";
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public String cancel(@PathVariable("orderId") Long orderId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        try {
            orderService.cancelOrder(memberId, orderId);
            redirectAttributes.addFlashAttribute("message", "주문을 취소했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }
}
