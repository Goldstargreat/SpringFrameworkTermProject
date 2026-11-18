package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 주문 엔티티
// ORDER 는 SQL 예약어(ORDER BY)라서 테이블 이름을 orders 로 지정한다
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 1명 : 주문 N건
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문 1건 : 주문 상품 N개
    // mappedBy = "order" : 외래키는 OrderItem.order 쪽에 있다는 뜻
    // cascade = ALL : 주문을 저장하면 주문 상품들도 함께 저장된다
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();

    // 받는 사람 이름
    @Column(nullable = false, length = 30)
    private String receiverName;

    // 받는 사람 연락처
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    // 배송지 주소
    @Column(nullable = false, length = 200)
    private String address;

    // 총 결제 금액
    @Column(nullable = false)
    private int totalPrice;

    // 배송 상태. EnumType.STRING : DB에 "SHIPPED" 같은 글자로 저장된다
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    // 주문 일시
    private LocalDateTime orderedAt;

    public Order() {
    }

    // 주문에 주문 상품을 추가하면서, 주문 상품 쪽에도 "내 주문은 이것"이라고 알려 준다 (양방향 연결)
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
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

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }
}
