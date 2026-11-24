package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.service.InquiryService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 상품평 / 상품문의 컨트롤러 : 상품 상세 화면의 "상품평", "상품문의" 탭에서 보내는 요청을 받는다
// 쓰기/삭제는 로그인해야 하고(SecurityConfig 의 /reviews/**, /inquiries/**),
// 답변은 관리자만 할 수 있다(/admin/**)
// 처리가 끝나면 상품 상세 화면의 해당 탭 위치(#tab-reviews, #tab-qna)로 돌아간다
@Controller
public class ProductBoardController {

    private final ReviewService reviewService;
    private final InquiryService inquiryService;

    public ProductBoardController(ReviewService reviewService, InquiryService inquiryService) {
        this.reviewService = reviewService;
        this.inquiryService = inquiryService;
    }

    // ===== 상품평 =====

    // 상품평 쓰기
    @PostMapping("/reviews")
    public String writeReview(@RequestParam("productId") Long productId,
                              @RequestParam(name = "rating", defaultValue = "0") int rating,
                              @RequestParam("content") String content,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.write(loginMemberId(session), productId, rating, content);
            redirectAttributes.addFlashAttribute("message", "상품평을 등록했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId + "#tab-reviews";
    }

    // 상품평 삭제 (본인 또는 관리자)
    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("reviewId") Long reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Long productId = reviewService.delete(loginMemberId(session), isAdmin(session), reviewId);
            redirectAttributes.addFlashAttribute("message", "상품평을 삭제했습니다.");
            return "redirect:/products/" + productId + "#tab-reviews";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // ===== 상품문의 =====

    // 문의 쓰기
    @PostMapping("/inquiries")
    public String ask(@RequestParam("productId") Long productId,
                      @RequestParam("question") String question,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        try {
            inquiryService.ask(loginMemberId(session), productId, question);
            redirectAttributes.addFlashAttribute("message", "문의를 등록했습니다. 답변이 달리면 이 화면에서 확인할 수 있습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId + "#tab-qna";
    }

    // 문의 삭제 (본인 또는 관리자)
    @PostMapping("/inquiries/{inquiryId}/delete")
    public String deleteInquiry(@PathVariable("inquiryId") Long inquiryId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Long productId = inquiryService.delete(loginMemberId(session), isAdmin(session), inquiryId);
            redirectAttributes.addFlashAttribute("message", "문의를 삭제했습니다.");
            return "redirect:/products/" + productId + "#tab-qna";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // (관리자) 문의 답변 : 주소가 /admin/ 으로 시작하므로 AdminCheckInterceptor 가 관리자만 들여보낸다
    @PostMapping("/admin/inquiries/{inquiryId}/answer")
    public String answer(@PathVariable("inquiryId") Long inquiryId,
                         @RequestParam("answer") String answer,
                         @RequestParam("productId") Long productId,
                         RedirectAttributes redirectAttributes) {
        try {
            inquiryService.answer(inquiryId, answer);
            redirectAttributes.addFlashAttribute("message", "답변을 등록했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId + "#tab-qna";
    }

    private Long loginMemberId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
    }

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE));
    }
}
