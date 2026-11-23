package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// 회원 엔티티 : members 테이블과 1:1로 연결되는 클래스
@Entity
@Table(name = "members")
public class Member {

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

    // 전화번호
    @Column(length = 20)
    private String phone;

    // 기본 배송지 주소
    @Column(length = 200)
    private String address;

    // 추가 배송지 주소 (회사, 부모님 댁 등. 비어 있어도 된다)
    @Column(length = 200)
    private String secondAddress;

    // 권한 : "USER"(일반 회원) 또는 "ADMIN"(관리자)
    @Column(nullable = false, length = 10)
    private String role;

    // 가입 일시
    private LocalDateTime createdAt;

    // JPA 는 기본 생성자가 반드시 필요하다
    public Member() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
