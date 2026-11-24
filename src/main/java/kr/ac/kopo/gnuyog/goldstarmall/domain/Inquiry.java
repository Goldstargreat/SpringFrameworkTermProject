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

    // 질문한 회원이 답변 알림을 확인했는지
    // 관리자가 답변을 달거나 고치면 false 가 되고, 회원이 알림 화면을 보면 true 가 된다 (비어 있으면 확인 안 함)
    private Boolean answerRead;

    public Inquiry() {
    }

    // 답변이 달렸는지 (화면에서 inquiry.answered 로 사용)
    public boolean isAnswered() {
        return answer != null;
    }

    // 아직 확인하지 않은 답변이 있는지 (회원 알림)
    public boolean isAnswerUnread() {
        return answer != null && !Boolean.TRUE.equals(answerRead);
    }

    public void setAnswerRead(boolean answerRead) {
        this.answerRead = answerRead;
    }

    // 질문한 회원이 답변 알림을 알림 목록에서 삭제했는지 (문의와 답변 자체는 그대로 남는다)
    // 관리자가 답변을 고치면 새 알림이 되므로 다시 false 가 된다 (비어 있으면 삭제 안 함)
    private Boolean answerNoticeHidden;

    public boolean isAnswerNoticeHidden() {
        return Boolean.TRUE.equals(answerNoticeHidden);
    }

    public void setAnswerNoticeHidden(boolean answerNoticeHidden) {
        this.answerNoticeHidden = answerNoticeHidden;
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
