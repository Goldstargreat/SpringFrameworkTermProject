package kr.ac.kopo.gnuyog.goldstarmall.domain;

// 배송 상태 : 실제 물류 흐름(주문접수 → 입고 → 출고 → 배송중 → 배송완료)을 그대로 옮겼다
public enum ShippingStatus {

    ORDER_RECEIVED("주문접수", 1),
    WAREHOUSED("입고", 2),
    SHIPPED("출고", 3),
    IN_DELIVERY("배송중", 4),
    DELIVERED("배송완료", 5),
    CANCELED("주문취소", 0),     // 손님이 직접 취소 (주문접수 단계에서만)
    REJECTED("주문거절", 0);     // 관리자가 거절 (출고 전까지, 거절 사유를 남긴다)

    // 화면에 보여 줄 한글 이름
    private final String label;

    // 진행 단계 번호 (1~5). 취소는 0
    private final int step;

    ShippingStatus(String label, int step) {
        this.label = label;
        this.step = step;
    }

    public String getLabel() {
        return label;
    }

    public int getStep() {
        return step;
    }

    // 다음 단계의 상태를 돌려준다. 더 진행할 수 없으면(배송완료, 주문취소) 자기 자신을 돌려준다
    public ShippingStatus next()
    {
        if (this == ORDER_RECEIVED)
        {
            return WAREHOUSED;
        } else if (this == WAREHOUSED)
        {
            return SHIPPED;
        } else if (this == SHIPPED)
        {
            return IN_DELIVERY;
        } else if (this == IN_DELIVERY)
        {
            return DELIVERED;
        }
        return this;
    }

    // 더 이상 진행할 게 없는 끝난 주문인지 (배송완료, 주문취소, 주문거절)
    // 화면(Thymeleaf)에서는 ${order.status.finished} 로 쓸 수 있다
    public boolean isFinished()
    {
        return this == DELIVERED || this == CANCELED || this == REJECTED;
    }

    // 취소되거나 거절되어 판매가 없던 일이 된 주문인지 (환불 대상, 판매량/매출에서 뺀다)
    // 화면에서는 ${order.status.voided} 로 쓸 수 있다
    public boolean isVoided()
    {
        return this == CANCELED || this == REJECTED;
    }

    // 관리자가 거절할 수 있는 단계인지 : 아직 출고되기 전(주문접수, 입고)
    // 화면에서는 ${order.status.rejectable} 로 쓸 수 있다
    public boolean isRejectable()
    {
        return this == ORDER_RECEIVED || this == WAREHOUSED;
    }
}
