package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 주문 상품 엔티티 : order_items 테이블
// 주문(Order)과 상품(Product)을 이어 주는 중간 테이블 역할을 한다
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 1건 : 주문 상품 N개
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 상품 1개 : 주문 상품 N개
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 당시의 가격 (나중에 상품 가격이 바뀌어도 주문 내역은 그대로 남도록 따로 저장)
    @Column(nullable = false)
    private int orderPrice;

    // 주문 수량
    @Column(nullable = false)
    private int quantity;

    // 주문한 옵션 (옵션이 없는 상품이면 null). 취소/거절/반품 때 이 옵션의 재고를 돌려놓는다
    @ManyToOne
    @JoinColumn(name = "option_id")
    private ProductOption option;

    public OrderItem() {
    }

    public ProductOption getOption() {
        return option;
    }

    public void setOption(ProductOption option) {
        this.option = option;
    }

    // 이 주문 상품의 금액 = 주문 당시 가격 x 수량
    public int getTotalPrice() {
        return orderPrice * quantity;
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
