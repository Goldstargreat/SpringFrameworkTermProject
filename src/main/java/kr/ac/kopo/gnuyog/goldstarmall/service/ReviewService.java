package kr.ac.kopo.gnuyog.goldstarmall.service;

import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Product;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Review;
import kr.ac.kopo.gnuyog.goldstarmall.domain.ShippingStatus;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.exception.NotFoundException;
import kr.ac.kopo.gnuyog.goldstarmall.repository.MemberRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.OrderItemRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ProductRepository;
import kr.ac.kopo.gnuyog.goldstarmall.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 상품평 서비스 : 상품평 조회, 작성, 삭제
// 규칙 : 그 상품을 주문해서 "배송완료"된 회원만, 상품 하나에 한 번만 쓸 수 있다
@Service
@Transactional
public class ReviewService {

    // 상품평 내용 최대 글자 수 (Review 의 content 컬럼 길이)
    public static final int CONTENT_MAX_LENGTH = 500;

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         MemberRepository memberRepository,
                         ProductRepository productRepository,
                         OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // (관리자) 모든 상품평 (최신순). pendingOnly 면 코멘트를 아직 안 단 것만
    @Transactional(readOnly = true)
    public List<Review> findAllForAdmin(boolean pendingOnly) {
        return pendingOnly ? reviewRepository.findByAdminCommentIsNullOrderByIdDesc()
                           : reviewRepository.findAllByOrderByIdDesc();
    }

    // (관리자) 코멘트를 아직 안 단 상품평 개수
    @Transactional(readOnly = true)
    public long countPending() {
        return reviewRepository.countByAdminCommentIsNull();
    }

    // 한 상품의 상품평 목록 (최신순)
    @Transactional(readOnly = true)
    public List<Review> findReviews(Product product) {
        return reviewRepository.findByProductOrderByIdDesc(product);
    }

    // 한 상품의 평균 별점 (소수점 첫째 자리까지, 상품평이 없으면 0)
    @Transactional(readOnly = true)
    public double averageRating(Product product) {
        Double avg = reviewRepository.averageRating(product);
        if (avg == null) {
            return 0;
        }
        return Math.round(avg * 10) / 10.0;
    }

    // 이 회원이 이 상품에 상품평을 쓸 수 없는 이유 (쓸 수 있으면 null)
    // 상품 상세 화면에서 작성 폼을 보여 줄지, 안내 문구를 보여 줄지 정하는 데 쓴다
    @Transactional(readOnly = true)
    public String cannotWriteReason(Long memberId, Product product) {
        if (memberId == null) {
            return "로그인한 회원만 상품평을 쓸 수 있습니다.";
        }
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return "로그인한 회원만 상품평을 쓸 수 있습니다.";
        }
        if ("ADMIN".equals(member.getRole())) {
            return "관리자는 상품평을 쓸 수 없습니다. 상품평에 코멘트를 달아 주세요.";
        }
        if (reviewRepository.existsByMemberAndProduct(member, product)) {
            return "이 상품의 상품평을 이미 작성했습니다.";
        }
        if (!orderItemRepository.existsOrderedBy(member, product, ShippingStatus.DELIVERED)) {
            return "이 상품을 주문해서 배송완료된 회원만 상품평을 쓸 수 있습니다.";
        }
        return null;
    }

    // 상품평 쓰기
    public Review write(Long memberId, Long productId, int rating, String content) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        // 1) 쓸 수 있는 회원인지 (화면에서 막았더라도 서버에서 한 번 더 확인한다)
        String reason = cannotWriteReason(memberId, product);
        if (reason != null) {
            throw new BusinessException(reason);
        }
        // 2) 입력값 검사
        if (rating < 1 || rating > 5) {
            throw new BusinessException("별점은 1점부터 5점까지 고를 수 있습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("상품평 내용을 입력해 주세요.");
        }
        if (content.trim().length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("상품평은 " + CONTENT_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }

        Review review = new Review();
        review.setMember(memberRepository.findById(memberId).orElseThrow());
        review.setProduct(product);
        review.setRating(rating);
        review.setContent(content.trim());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // (관리자) 코멘트 달기 : 이미 코멘트가 있으면 새 코멘트로 고친다. 상품 번호를 돌려준다
    public Long comment(Long reviewId, String comment) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            throw new NotFoundException("상품평을 찾을 수 없습니다.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new BusinessException("코멘트 내용을 입력해 주세요.");
        }
        if (comment.trim().length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("코멘트는 " + CONTENT_MAX_LENGTH + "글자까지 쓸 수 있습니다.");
        }
        review.setAdminComment(comment.trim());
        review.setCommentedAt(LocalDateTime.now());
        return review.getProduct().getId();
    }

    // 상품평 삭제 : 쓴 사람 본인이나 관리자만 지울 수 있다. 지운 상품평의 상품 번호를 돌려준다
    public Long delete(Long memberId, boolean admin, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            throw new NotFoundException("상품평을 찾을 수 없습니다.");
        }
        if (!admin && !review.getMember().getId().equals(memberId)) {
            throw new BusinessException("본인이 쓴 상품평만 삭제할 수 있습니다.");
        }
        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        return productId;
    }
}
