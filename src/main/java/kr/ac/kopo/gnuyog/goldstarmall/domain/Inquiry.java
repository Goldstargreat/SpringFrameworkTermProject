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

// 상품문의 엔티티 : inquiries 테이블
// 회원이 상품에 대해 질문(question)을 남기면, 관리자가 답변(answer)을 단다
@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 질문한 회원
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 어떤 상품에 대한 문의인지
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 질문 내용
    @Column(nullable = false, length = 500)
    private String question;

    // 답변 내용 : 관리자가 답변하기 전에는 비어 있다(null)
    @Column(length = 500)
    private String answer;

    // 질문한 날짜
    private LocalDateTime createdAt;

    // 답변한 날짜
    private LocalDateTime answeredAt;

    public Inquiry() {
    }

    // 답변이 달렸는지 (화면에서 inquiry.answered 로 사용)
    public boolean isAnswered() {
        return answer != null;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
