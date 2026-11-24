package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
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
import java.util.Map;

// 관리자 컨트롤러 : 주문관리(배송 상태 변경, 주문 삭제), 회원관리(회원 정보 수정, 삭제), 상품관리(등록, 가격/재고 수정)
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
        } catch (BusinessException e) {
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
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // 회원 목록 : 검색어(keyword)가 있으면 검색 기준(type)에 따른 검색 결과, 없으면 전체 회원
    // type : all(전체), name(이름), loginId(아이디), phone(전화번호)
    @GetMapping("/members")
    public String members(@RequestParam(name = "type", defaultValue = "all") String type,
                          @RequestParam(name = "keyword", required = false) String keyword,
                          Model model) {
        List<Member> members = memberService.searchMembers(type, keyword);
        model.addAttribute("members", members);
        // 검색창에 검색 기준과 검색어를 그대로 남겨 두고, 결과 건수를 보여 주는 데 쓴다
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        return "admin/members";
    }

    // 회원 정보 수정 화면
    // 이름, 전화번호, 배송지만 고칠 수 있다
    // (회원번호는 주문/장바구니가 가리키는 기본키라서, 아이디는 로그인에 쓰는 값이라서 바꿀 수 없다)
    @GetMapping("/members/{memberId}/edit")
    public String editMemberForm(@PathVariable("memberId") Long memberId, Model model, RedirectAttributes redirectAttributes) {
        Member member = memberService.findById(memberId);
        if (member == null) {
            redirectAttributes.addFlashAttribute("error", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        model.addAttribute("member", member);
        return "admin/member-edit";
    }

    // 회원 정보 수정 처리 : 회원이 마이페이지에서 고칠 때와 같은 검사(이름 필수, 전화번호 형식/중복)를 한다
    @PostMapping("/members/{memberId}/edit")
    public String editMember(@PathVariable("memberId") Long memberId,
                             @RequestParam("name") String name,
                             @RequestParam("phone") String phone,
                             @RequestParam(name = "address", required = false) String address,
                             @RequestParam(name = "secondAddress", required = false) String secondAddress,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (memberService.findById(memberId) == null) {
            redirectAttributes.addFlashAttribute("error", "회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        try {
            memberService.updateInfo(memberId, name, phone, address, secondAddress);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/members/" + memberId + "/edit";
        }
        // 관리자가 자기 이름을 고쳤다면 화면 상단 "OOO님" 도 바뀌도록 세션 값도 바꾼다
        if (memberId.equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ID))) {
            session.setAttribute(SessionConst.LOGIN_MEMBER_NAME, name.trim());
        }
        redirectAttributes.addFlashAttribute("message", memberId + "번 회원 정보를 수정했습니다.");
        return "redirect:/admin/members";
    }

    // 회원 삭제 (회원의 장바구니, 주문 내역도 함께 삭제된다)
    @PostMapping("/members/{memberId}/delete")
    public String deleteMember(@PathVariable("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        try {
            memberService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("message", memberId + "번 회원을 삭제했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members";
    }

    // 상품관리 화면 : 전체 상품 목록(가격/재고 수정, 누적 판매량/매출) + 새 상품 등록
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findProducts(null, null));
        model.addAttribute("categories", productService.getCategories());

        // 누적 판매량/매출 : 상품 번호로 찾아 쓴다 (화면에서 salesQuantity[상품번호])
        Map<Long, Long> salesQuantity = productService.salesQuantityByProduct();
        Map<Long, Long> salesAmount = productService.salesAmountByProduct();
        model.addAttribute("salesQuantity", salesQuantity);
        model.addAttribute("salesAmount", salesAmount);
        // 표 위에 보여 줄 전체 합계
        model.addAttribute("totalSalesQuantity", salesQuantity.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("totalSalesAmount", salesAmount.values().stream().mapToLong(Long::longValue).sum());
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
                                @RequestParam(name = "manufacturer", required = false) String manufacturer,
                                @RequestParam(name = "origin", required = false) String origin,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.create(name, category, price, stock, description, icon, manufacturer, origin);
            redirectAttributes.addFlashAttribute("message", product.getId() + "번 상품 '" + product.getName() + "'을(를) 등록했습니다.");
        } catch (BusinessException e) {
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
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // 상품 삭제 (주문된 적 없는 상품만)
    @PostMapping("/products/{productId}/delete")
    public String deleteProduct(@PathVariable("productId") Long productId, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("message", productId + "번 상품을 삭제했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
