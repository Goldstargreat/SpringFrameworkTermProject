package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Inquiry;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Review;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.InquiryRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// 회원 알림 서비스 : 내 상품문의에 답변이 달리거나, 내 상품평에 관리자 코멘트가 달리면 알림으로 보여 준다
// - 확인하지 않은 알림 개수 : 화면 상단 "알림 N" (MemberNoticeAdvice)
// - 알림 화면을 열면 그때까지의 알림을 모두 "확인함"으로 바꾼다
// - 관리자가 답변/코멘트를 고치면 다시 새 알림이 된다 (InquiryService.answer, ReviewService.comment)
// - 확인한 알림은 알림 목록에서 삭제할 수 있다 (문의/답변, 상품평/코멘트 자체는 그대로 남고 알림만 숨긴다)
@Service
@Transactional
public class NotificationService {

    // 알림 종류 : 삭제 주소(/notifications/{kind}/{id}/delete)에 쓴다
    public static final String KIND_INQUIRY = "inquiry";
    public static final String KIND_REVIEW = "review";

    // 오류 메시지
    private static final String NOTICE_NOT_FOUND = "알림을 찾을 수 없습니다.";
    private static final String NOTICE_UNREAD = "아직 확인하지 않은 알림은 삭제할 수 없습니다.";

    private final InquiryRepository inquiryRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    public NotificationService(InquiryRepository inquiryRepository, ReviewRepository reviewRepository,
                               MemberRepository memberRepository) {
        this.inquiryRepository = inquiryRepository;
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
    }

    // ===== 알림 보기 =====

    // 확인하지 않은 알림 개수 (답변 + 코멘트)
    @Transactional(readOnly = true)
    public long countUnread(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return 0;
        }
        return inquiryRepository.countUnreadAnswers(member) + reviewRepository.countUnreadComments(member);
    }

    // 알림 목록을 만들고(최신순), 모두 "확인함"으로 바꾼다
    // 화면에 "새 알림" 표시를 하려면 확인함으로 바꾸기 전의 상태가 필요하므로, 먼저 Notification 으로 옮겨 담는다
    public List<Notification> readAll(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        List<Notification> list = new ArrayList<>();

        // 1) 문의 답변 알림
        for (Inquiry inquiry : inquiryRepository.findAnswerNotices(member)) {
            list.add(fromInquiry(inquiry));
            inquiry.setAnswerRead(true);
        }

        // 2) 상품평 코멘트 알림
        for (Review review : reviewRepository.findCommentNotices(member)) {
            list.add(fromReview(review));
            review.setCommentRead(true);
        }

        // 3) 답변/코멘트가 달린 날짜 최신순 (날짜가 없는 옛 데이터는 맨 뒤)
        list.sort(Comparator.comparing(Notification::getFeedbackAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    // 문의 → 알림 한 건 (상품 상세의 Q&A 탭으로 간다)
    private Notification fromInquiry(Inquiry inquiry) {
        Product product = inquiry.getProduct();
        return new Notification(KIND_INQUIRY, inquiry.getId(), "문의 답변",
                product.getId(), product.getIcon(), product.getName(), "#tab-qna",
                inquiry.getQuestion(), inquiry.getAnswer(), inquiry.getAnsweredAt(),
                inquiry.isAnswerUnread());
    }

    // 상품평 → 알림 한 건 (상품 상세의 상품평 탭으로 간다)
    private Notification fromReview(Review review) {
        Product product = review.getProduct();
        return new Notification(KIND_REVIEW, review.getId(), "상품평 코멘트",
                product.getId(), product.getIcon(), product.getName(), "#tab-reviews",
                review.getContent(), review.getAdminComment(), review.getCommentedAt(),
                review.isCommentUnread());
    }

    // ===== 알림 삭제 =====

    // 알림 1건 삭제 : 내 알림이고, 확인한 알림만 지울 수 있다
    public void hide(Long memberId, String kind, Long id) {
        if (KIND_INQUIRY.equals(kind)) {
            hideInquiryNotice(memberId, id);
        } else if (KIND_REVIEW.equals(kind)) {
            hideReviewNotice(memberId, id);
        } else {
            throw new NotFoundException(NOTICE_NOT_FOUND);
        }
    }

    // 문의 답변 알림 1건 삭제
    private void hideInquiryNotice(Long memberId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);

        // 없는 문의, 답변이 없는 문의, 이미 지운 알림, 남의 문의는 "없는 알림"으로 본다
        boolean notMyNotice = inquiry == null
                || inquiry.getAnswer() == null
                || inquiry.isAnswerNoticeHidden()
                || !inquiry.getMember().getId().equals(memberId);
        if (notMyNotice) {
            throw new NotFoundException(NOTICE_NOT_FOUND);
        }
        if (inquiry.isAnswerUnread()) {
            throw new BusinessException(NOTICE_UNREAD);
        }
        inquiry.setAnswerNoticeHidden(true);
    }

    // 상품평 코멘트 알림 1건 삭제
    private void hideReviewNotice(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);

        // 없는 상품평, 코멘트가 없는 상품평, 이미 지운 알림, 남의 상품평은 "없는 알림"으로 본다
        boolean notMyNotice = review == null
                || review.getAdminComment() == null
                || review.isCommentNoticeHidden()
                || !review.getMember().getId().equals(memberId);
        if (notMyNotice) {
            throw new NotFoundException(NOTICE_NOT_FOUND);
        }
        if (review.isCommentUnread()) {
            throw new BusinessException(NOTICE_UNREAD);
        }
        review.setCommentNoticeHidden(true);
    }

    // 확인한 알림 전체 삭제 : 지운 개수를 돌려준다 (그 사이 새로 온 확인 안 한 알림은 남긴다)
    public int hideAllRead(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        int count = 0;

        for (Inquiry inquiry : inquiryRepository.findAnswerNotices(member)) {
            if (!inquiry.isAnswerUnread()) {
                inquiry.setAnswerNoticeHidden(true);
                count++;
            }
        }
        for (Review review : reviewRepository.findCommentNotices(member)) {
            if (!review.isCommentUnread()) {
                review.setCommentNoticeHidden(true);
                count++;
            }
        }
        return count;
    }

    // ===== 알림 한 건 =====

    // 화면에 보여 주기 위한 값만 담는다
    public static class Notification {
        private final String kind;          // inquiry / review (삭제할 때 쓴다)
        private final Long sourceId;        // 문의 번호 / 상품평 번호
        private final String type;          // 문의 답변 / 상품평 코멘트
        private final Long productId;
        private final String productIcon;
        private final String productName;
        private final String anchor;        // 상품 상세의 어느 탭으로 갈지 (#tab-qna, #tab-reviews)
        private final String myText;        // 내가 쓴 질문 / 상품평
        private final String feedback;      // 관리자 답변 / 코멘트
        private final LocalDateTime feedbackAt;
        private final boolean unread;       // 이번에 처음 보는 알림인지

        public Notification(String kind, Long sourceId, String type,
                            Long productId, String productIcon, String productName, String anchor,
                            String myText, String feedback, LocalDateTime feedbackAt, boolean unread) {
            this.kind = kind;
            this.sourceId = sourceId;
            this.type = type;
            this.productId = productId;
            this.productIcon = productIcon;
            this.productName = productName;
            this.anchor = anchor;
            this.myText = myText;
            this.feedback = feedback;
            this.feedbackAt = feedbackAt;
            this.unread = unread;
        }

        public String getKind() { return kind; }
        public Long getSourceId() { return sourceId; }
        public String getType() { return type; }
        public Long getProductId() { return productId; }
        public String getProductIcon() { return productIcon; }
        public String getProductName() { return productName; }
        public String getAnchor() { return anchor; }
        public String getMyText() { return myText; }
        public String getFeedback() { return feedback; }
        public LocalDateTime getFeedbackAt() { return feedbackAt; }
        public boolean isUnread() { return unread; }
    }
}
