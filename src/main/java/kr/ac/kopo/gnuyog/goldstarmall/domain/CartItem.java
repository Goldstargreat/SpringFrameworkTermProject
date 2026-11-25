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

    // 고른 옵션 (옵션이 없는 상품이면 null). 같은 상품이라도 옵션이 다르면 다른 항목이다
    @ManyToOne
    @JoinColumn(name = "option_id")
    private ProductOption option;

    public CartItem() {
    }

    // ===== 금액 / 재고 =====

    // 이 항목의 금액 = 오늘 판매 가격(오늘의 할인 적용) x 수량 (화면에서 cartItem.totalPrice 로 사용)
    public int getTotalPrice() {
        return product.getSalePrice() * quantity;
    }

    // 담을 수 있는 재고 : 옵션을 골랐으면 그 옵션의 재고, 아니면 상품 재고
    public int getAvailableStock() {
        if (option != null) {
            return option.getStock();
        }
        return product.getStock();
    }

    // 재고가 부족할 때 보여 줄 문구 : 옵션을 골랐으면 그 옵션의 문구, 아니면 상품의 문구
    public String getStockShortageMessage() {
        if (option != null) {
            return option.getStockShortageMessage();
        }
        return product.getStockShortageMessage();
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

    public ProductOption getOption() {
        return option;
    }

    public void setOption(ProductOption option) {
        this.option = option;
    }
}
