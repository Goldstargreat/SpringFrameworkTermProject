package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 관리자 컨트롤러 : 물류센터 직원이 배송 상태를 한 단계씩 넘기는 화면
// AdminCheckInterceptor 때문에 관리자(ADMIN)만 들어올 수 있다
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;

    public AdminController(OrderService orderService) {
        this.orderService = orderService;
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
}
