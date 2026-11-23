package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 관리자 컨트롤러 : 배송관리(배송 상태 변경, 주문 삭제), 회원관리(회원 삭제), 상품관리(등록, 가격/재고 수정)
// AdminCheckInterceptor 때문에 관리자(ADMIN)만 들어올 수 있다
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ProductService productService;

    public AdminController(OrderService orderService, MemberService memberService, ProductService productService) {
        this.orderService = orderService;
        this.memberService = memberService;
        this.productService = productService;
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

    // 상품관리 화면 : 전체 상품 목록(가격/재고 수정) + 새 상품 등록
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findProducts(null, null));
        model.addAttribute("categories", productService.getCategories());
        return "admin/products";
    }

    // 새 상품 등록
    @PostMapping("/products")
    public String createProduct(@RequestParam("name") String name,
                                @RequestParam("category") String category,
                                @RequestParam("price") int price,
                                @RequestParam("stock") int stock,
                                @RequestParam(name = "description", required = false) String description,
                                @RequestParam(name = "icon", required = false) String icon,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.create(name, category, price, stock, description, icon);
            redirectAttributes.addFlashAttribute("message", product.getId() + "번 상품 '" + product.getName() + "'을(를) 등록했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // 가격, 재고 수정
    @PostMapping("/products/{productId}/update")
    public String updateProduct(@PathVariable("productId") Long productId,
                                @RequestParam("price") int price,
                                @RequestParam("stock") int stock,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.updatePriceAndStock(productId, price, stock);
            redirectAttributes.addFlashAttribute("message", productId + "번 상품의 가격과 재고를 수정했습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
