package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Inquiry;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Review;
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
@Service
@Transactional
public class NotificationService {

    private final InquiryRepository inquiryRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    public NotificationService(InquiryRepository inquiryRepository, ReviewRepository reviewRepository,
                               MemberRepository memberRepository) {
        this.inquiryRepository = inquiryRepository;
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
    }

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

        for (Inquiry q : inquiryRepository.findByMemberAndAnswerIsNotNullOrderByAnsweredAtDesc(member)) {
            list.add(new Notification("문의 답변", q.getProduct().getId(), q.getProduct().getIcon(),
                    q.getProduct().getName(), "#tab-qna", q.getQuestion(), q.getAnswer(), q.getAnsweredAt(),
                    q.isAnswerUnread()));
            q.setAnswerRead(true);
        }
        for (Review r : reviewRepository.findByMemberAndAdminCommentIsNotNullOrderByCommentedAtDesc(member)) {
            list.add(new Notification("상품평 코멘트", r.getProduct().getId(), r.getProduct().getIcon(),
                    r.getProduct().getName(), "#tab-reviews", r.getContent(), r.getAdminComment(), r.getCommentedAt(),
                    r.isCommentUnread()));
            r.setCommentRead(true);
        }

        // 답변/코멘트가 달린 날짜 최신순 (날짜가 없는 옛 데이터는 맨 뒤)
        list.sort(Comparator.comparing(Notification::getFeedbackAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    // 알림 한 건 (화면에 보여 주기 위한 값만 담는다)
    public static class Notification {
        private final String type;          // 문의 답변 / 상품평 코멘트
        private final Long productId;
        private final String productIcon;
        private final String productName;
        private final String anchor;        // 상품 상세의 어느 탭으로 갈지 (#tab-qna, #tab-reviews)
        private final String myText;        // 내가 쓴 질문 / 상품평
        private final String feedback;      // 관리자 답변 / 코멘트
        private final LocalDateTime feedbackAt;
        private final boolean unread;       // 이번에 처음 보는 알림인지

        public Notification(String type, Long productId, String productIcon, String productName, String anchor,
                            String myText, String feedback, LocalDateTime feedbackAt, boolean unread) {
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
