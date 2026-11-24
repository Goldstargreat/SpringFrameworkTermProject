package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// 상품평 엔티티 : reviews 테이블
// "어떤 회원이 어떤 상품에 별점 몇 점과 어떤 글을 남겼는지" 한 줄이 상품평 하나다
// 상품을 주문해서 배송완료된 회원만, 상품 하나에 한 번만 쓸 수 있다 (ReviewService 참고)
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 쓴 사람 (회원 1명 : 상품평 N개)
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 어떤 상품의 상품평인지 (상품 1개 : 상품평 N개)
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 별점 1 ~ 5
    @Column(nullable = false)
    private int rating;

    // 상품평 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 쓴 날짜
    private LocalDateTime createdAt;

    // 관리자 코멘트 : 관리자가 코멘트를 달기 전에는 비어 있다(null)
    @Column(length = 500)
    private String adminComment;

    // 관리자가 코멘트를 단 날짜
    private LocalDateTime commentedAt;

    public Review() {
    }

    // 관리자 코멘트가 달렸는지 (화면에서 review.commented 로 사용)
    public boolean isCommented() {
        return adminComment != null;
    }

    // 화면에 별 모양으로 보여 주기 위한 문자열 (예: 4점 → ★★★★☆)
    public String getStars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
    }
}
