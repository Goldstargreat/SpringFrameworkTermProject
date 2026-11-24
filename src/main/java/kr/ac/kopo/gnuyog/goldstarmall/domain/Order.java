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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 주문 엔티티
// ORDER 는 SQL 예약어(ORDER BY)라서 테이블 이름을 orders 로 지정한다
@Entity
@Table(name = "orders")
public class Order {

    // 주문 id : DB가 자동으로 붙이는 기본키. 주문 상품(order_items)이 이 값으로 주문을 가리키므로 바꾸지 않는다
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문번호 : 화면에 보여 주는 번호 (예: 20261124-0012). 주문할 때 자동으로 붙고, 관리자가 고칠 수 있다
    // 기본키(id)와 따로 두었기 때문에 바꿔도 주문 상품과의 연결은 그대로다. 중복은 안 된다
    @Column(unique = true, length = 30)
    private String orderNumber;

    // 회원 1명 : 주문 N건
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문 1건 : 주문 상품 N개
    // mappedBy = "order" : 외래키는 OrderItem.order 쪽에 있다는 뜻
    // cascade = ALL : 주문을 저장하면 주문 상품들도 함께 저장된다
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 받는 사람 이름
    @Column(nullable = false, length = 30)
    private String receiverName;

    // 받는 사람 연락처
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    // 배송지 주소 (회원의 기본 주소와 따로, 주문마다 저장한다) → shipping_address 컬럼
    @Column(nullable = false, length = 200)
    private String shippingAddress;

    // 총 결제 금액
    @Column(nullable = false)
    private int totalPrice;

    // 배송 상태. EnumType.STRING : DB에 "SHIPPED" 같은 글자로 저장된다
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShippingStatus status;

    // 주문 일시
    private LocalDateTime orderedAt;

    // 환불 금액 : 주문취소/주문거절 때 결제 금액 전체를 돌려준다 (환불 전에는 null)
    // 실제 결제(카드, 계좌) 기능이 없으므로 "얼마를 언제 환불했는지"를 기록해 화면에 보여 준다
    private Integer refundAmount;

    // 환불 일시
    private LocalDateTime refundedAt;

    // 거절 사유 : 관리자가 주문을 거절할 때 남긴다 (손님에게 보여 준다)
    @Column(length = 200)
    private String rejectReason;

    public Order() {
    }

    // 자동으로 붙이는 기본 주문번호 : 주문 날짜 + "-" + id 4자리 (예: 2026-11-24 의 12번 주문 → 20261124-0012)
    public String makeDefaultOrderNumber() {
        String date = orderedAt == null ? "00000000" : orderedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return date + "-" + String.format("%04d", id);
    }

    // 화면에 보여 줄 주문번호 (화면에서 order.displayNumber 로 사용)
    // SQL 로 직접 넣은 주문처럼 아직 주문번호가 없으면 기본 주문번호를 보여 준다
    public String getDisplayNumber() {
        return orderNumber != null ? orderNumber : makeDefaultOrderNumber();
    }

    // 환불이 끝난 주문인지 (화면에서 order.refunded 로 사용)
    public boolean isRefunded() {
        return refundedAt != null;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public Integer getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Integer refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
