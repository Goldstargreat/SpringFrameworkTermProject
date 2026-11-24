package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Order;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ProductService;
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
    private final ProductService productService;
    private final MemberService memberService;

    public OrderController(OrderService orderService, ProductService productService, MemberService memberService) {
        this.orderService = orderService;
        this.productService = productService;
        this.memberService = memberService;
    }

    // 주문하기 : 장바구니 화면의 주문서에서 넘어온다
    @PostMapping
    public String create(@RequestParam("receiverName") String receiverName,
                         @RequestParam("receiverPhone") String receiverPhone,
                         @RequestParam("shippingAddress") String shippingAddress,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);

        // 받는 사람 정보가 비어 있으면 주문할 수 없다
        if (receiverName.trim().isEmpty() || receiverPhone.trim().isEmpty() || shippingAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "받는 사람, 연락처, 배송지를 모두 입력해 주세요.");
            return "redirect:/cart";
        }

        try {
            Order order = orderService.createOrder(memberId, receiverName.trim(), receiverPhone.trim(), shippingAddress.trim());
            redirectAttributes.addFlashAttribute("message", "주문이 완료되었습니다.");
            return "redirect:/orders/" + order.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    // ===== 바로 주문하기 (상품 상세 화면 → 주문서 → 주문 완료) =====

    // 바로 주문 주문서 화면 : 상품 상세 화면의 "바로 주문하기" 버튼으로 들어온다
    // 예) /orders/direct?productId=2&quantity=3
    @GetMapping("/direct")
    public String directForm(@RequestParam(name = "productId", required = false) Long productId,
                             @RequestParam(name = "quantity", required = false) String quantity,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Product product = productId == null ? null : productService.findById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "상품을 찾을 수 없습니다.");
            return "redirect:/";
        }
        int count = parseQuantity(quantity);
        try {
            orderService.checkDirectOrderQuantity(product, count);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }

        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        model.addAttribute("product", product);
        model.addAttribute("quantity", count);
        model.addAttribute("totalPrice", product.getPrice() * count);
        // 받는 사람 정보를 회원 정보로 미리 채우기 위해
        model.addAttribute("member", memberService.findById(memberId));
        return "order/direct";
    }

    // 바로 주문 처리
    @PostMapping("/direct")
    public String createDirect(@RequestParam("productId") Long productId,
                               @RequestParam("quantity") String quantity,
                               @RequestParam("receiverName") String receiverName,
                               @RequestParam("receiverPhone") String receiverPhone,
                               @RequestParam("shippingAddress") String shippingAddress,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
        int count = parseQuantity(quantity);
        // 실패하면 같은 주문서로 돌아간다
        String backToForm = "redirect:/orders/direct?productId=" + productId + "&quantity=" + count;

        if (receiverName.trim().isEmpty() || receiverPhone.trim().isEmpty() || shippingAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "받는 사람, 연락처, 배송지를 모두 입력해 주세요.");
            return backToForm;
        }

        try {
            Order order = orderService.createDirectOrder(memberId, productId, count,
                    receiverName.trim(), receiverPhone.trim(), shippingAddress.trim());
            redirectAttributes.addFlashAttribute("message", "주문이 완료되었습니다.");
            return "redirect:/orders/" + order.getId();
        } catch (BusinessException e) {
            // 재고 부족 등 : 상품 화면으로 돌아가 남은 재고를 다시 보게 한다
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    // 수량 문자열 → 숫자 (숫자가 아니면 0 : 이후 "1개 이상" 검사에서 걸린다)
    private int parseQuantity(String quantity) {
        try {
            return quantity == null ? 1 : Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            return 0;
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
        } catch (BusinessException e) {
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
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }
}
