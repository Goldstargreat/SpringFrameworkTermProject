package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// 회원 엔티티 : members 테이블과 1:1로 연결되는 클래스
@Entity
@Table(name = "members")
public class Member
{
    // 기본키(PK). MySQL의 AUTO_INCREMENT 로 번호가 자동 증가한다
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디 : 중복 불가(unique), 비어 있을 수 없음
    @Column(nullable = false, unique = true, length = 30)
    private String loginId;

    // 비밀번호 : BCrypt 로 암호화된 문자열이 저장된다 (원래 비밀번호는 저장하지 않음)
    @Column(nullable = false, length = 100)
    private String password;

    // 이름
    @Column(nullable = false, length = 30)
    private String name;

    // 전화번호 : 이름은 동명이인이 있을 수 있지만 전화번호는 사람마다 다르므로 중복 불가(unique)
    // 필수 입력 : 비어 있을 수 없음(nullable = false)
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    // 기본 배송지 주소
    @Column(length = 200)
    private String address;

    // 추가 배송지 주소 (회사, 학교 등. 비어 있어도 된다)
    @Column(length = 200)
    private String secondAddress;

    // 권한 : "USER"(일반 회원) 또는 "ADMIN"(관리자)
    @Column(nullable = false, length = 10)
    private String role;

    // 가입 일시
    private LocalDateTime createdAt;

    // 회원 등급 : 배송완료된 주문의 누적 결제 금액이 기준 금액을 넘으면 자동으로 정해진다 (MemberGrade 참고)
    // 관리자가 직접 바꾸지 않는다. 기준 금액과 할인율은 MemberGrade 에서 정한다
    // 이 기능이 생기기 전에 가입한 회원은 비어 있을(null) 수 있어서, getGrade() 에서 '일반'으로 본다
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MemberGrade grade;


    // JPA 는 기본 생성자가 반드시 필요하다
    public Member()
    {
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    // 가린 이름 : 상품평/상품문의에 글쓴이를 보여 줄 때 쓴다 (예: 홍길동 → 홍*동, 김철 → 김*)
    public String getMaskedName() {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSecondAddress() {
        return secondAddress;
    }

    public void setSecondAddress(String secondAddress) {
        this.secondAddress = secondAddress;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public MemberGrade getGrade() {
        return grade == null ? MemberGrade.NORMAL : grade;
    }

    public void setGrade(MemberGrade grade) {
        this.grade = grade;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
