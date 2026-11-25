package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.service.InquiryService;
import kr.ac.kopo.gnuyog.goldstarmall.service.OrderService;
import kr.ac.kopo.gnuyog.goldstarmall.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 관리자에게 "아직 처리하지 않은 문의/상품평 개수"를 모든 화면에 넣어 주는 클래스
// 화면 상단의 "리뷰/문의 3" 처럼 빨간 숫자로 보여 준다 (화면에서 ${adminPendingInquiries}, ${adminPendingReviews})
// 손님이 새로 문의하거나 상품평을 쓰면 다음 화면부터 숫자가 바로 올라간다
@ControllerAdvice
public class AdminNoticeAdvice {

    private final InquiryService inquiryService;
    private final ReviewService reviewService;
    private final OrderService orderService;

    public AdminNoticeAdvice(InquiryService inquiryService, ReviewService reviewService, OrderService orderService) {
        this.inquiryService = inquiryService;
        this.reviewService = reviewService;
        this.orderService = orderService;
    }

    // 답변 대기 문의 개수 (관리자가 아니면 null)
    @ModelAttribute("adminPendingInquiries")
    public Long adminPendingInquiries(HttpSession session) {
        return isAdmin(session) ? inquiryService.countPending() : null;
    }

    // 코멘트 대기 상품평 개수 (관리자가 아니면 null)
    @ModelAttribute("adminPendingReviews")
    public Long adminPendingReviews(HttpSession session) {
        return isAdmin(session) ? reviewService.countPending() : null;
    }

    // 처리 대기 반품 신청 개수 (관리자가 아니면 null) → 화면 상단 '주문관리' 옆 빨간 숫자
    @ModelAttribute("adminPendingReturns")
    public Long adminPendingReturns(HttpSession session) {
        return isAdmin(session) ? orderService.countPendingReturns() : null;
    }

    private boolean isAdmin(HttpSession session) {
        return Member.ROLE_ADMIN.equals(session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE));
    }
}
