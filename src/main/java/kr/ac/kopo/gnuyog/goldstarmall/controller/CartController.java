package kr.ac.kopo.gnuyog.goldstarmall.controller;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.CartItem;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.service.CartService;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 장바구니 컨트롤러 : 장바구니 화면, 담기, 수량 변경, 항목 삭제
// LoginCheckInterceptor 때문에 로그인한 사람만 들어올 수 있다 (로그인한 회원 번호는 세션에 들어 있다)
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final MemberService memberService;

    public CartController(CartService cartService, MemberService memberService) {
        this.cartService = cartService;
        this.memberService = memberService;
    }

    // 장바구니 화면
    @GetMapping
    public String cart(HttpSession session, Model model) {
        Long memberId = loginMemberId(session);

        // 1) 담은 상품과 상품 금액 합계
        List<CartItem> cartItems = cartService.findCartItems(memberId);
        int originalPrice = cartService.calculateTotalPrice(cartItems);

        // 2) 회원 : 주문서의 받는 사람 칸에 회원 정보를 미리 채워 주기 위해 함께 넘긴다
        Member member = memberService.findById(memberId);

        // 3) 등급 할인 : 상품 금액 합계, 할인 금액, 결제 금액을 나눠서 보여 준다 (실제 계산은 주문할 때 OrderService 가 한다)
        int discount = member.getGrade().discountOf(originalPrice);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("member", member);
        model.addAttribute("originalPrice", originalPrice);
        model.addAttribute("discount", discount);
        model.addAttribute("totalPrice", originalPrice - discount);
        return "cart/list";
    }

    // 장바구니에 담기
    @PostMapping("/add")
    public String add(@RequestParam("productId") Long productId,
                      @RequestParam(name = "optionId", required = false) Long optionId,
                      @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(loginMemberId(session), productId, optionId, quantity);
            redirectAttributes.addFlashAttribute("message", "장바구니에 담았습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }
        return "redirect:/cart";
    }

    // 수량 변경
    @PostMapping("/{cartItemId}/update")
    public String update(@PathVariable("cartItemId") Long cartItemId,
                         @RequestParam("quantity") int quantity,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(loginMemberId(session), cartItemId, quantity);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // 항목 삭제
    @PostMapping("/{cartItemId}/delete")
    public String delete(@PathVariable("cartItemId") Long cartItemId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.deleteCartItem(loginMemberId(session), cartItemId);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // 로그인한 회원 번호 (세션에 들어 있다)
    private Long loginMemberId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
    }
}
