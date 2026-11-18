package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 장바구니 항목 엔티티 : cart_items 테이블
// "어떤 회원이 어떤 상품을 몇 개 담았는지" 한 줄이 한 항목이다
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 1명 : 장바구니 항목 N개  →  항목 쪽에서 보면 다대일(ManyToOne)
    // member_id 컬럼이 members 테이블의 id 를 가리키는 외래키(FK)가 된다
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 상품 1개 : 장바구니 항목 N개
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 담은 수량
    @Column(nullable = false)
    private int quantity;

    public CartItem() {
    }

    // 이 항목의 금액 = 상품 가격 x 수량 (화면에서 cartItem.totalPrice 로 사용)
    public int getTotalPrice() {
        return product.getPrice() * quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
