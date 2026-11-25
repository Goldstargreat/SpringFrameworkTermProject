package kr.ac.kopo.gnuyog.goldstarmall.domain;

// 배송 상태 : 실제 물류 흐름(주문접수 → 입고 → 출고 → 배송중 → 배송완료)을 그대로 옮겼다
public enum ShippingStatus {

    ORDER_RECEIVED("주문접수", 1),
    WAREHOUSED("입고", 2),
    SHIPPED("출고", 3),
    IN_DELIVERY("배송중", 4),
    DELIVERED("배송완료", 5),
    CANCELED("주문취소", 0),     // 손님이 직접 취소 (배송완료 전까지)
    REJECTED("주문거절", 0),     // 관리자가 거절 (출고 전까지, 거절 사유를 남긴다)
    RETURN_REQUESTED("반품신청", 5), // 손님이 배송완료 후 14일 이내 반품 신청 (관리자가 검수 후 승인/거절)
    RETURNED("반품완료", 0);     // 관리자가 반품을 승인 (결제 금액 전체 환불)

    // 화면에 보여 줄 한글 이름
    private final String label;

    // 진행 단계 번호 (1~5). 취소/거절/반품완료는 0, 반품신청은 배송완료까지 마친 상태라 5
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

    // ===== 다음 단계 =====

    // 다음 단계의 상태를 돌려준다. 더 진행할 수 없으면(배송완료, 주문취소, 반품 등) 자기 자신을 돌려준다
    public ShippingStatus next() {
        switch (this) {
            case ORDER_RECEIVED:
                return WAREHOUSED;
            case WAREHOUSED:
                return SHIPPED;
            case SHIPPED:
                return IN_DELIVERY;
            case IN_DELIVERY:
                return DELIVERED;
            default:
                return this;
        }
    }

    // ===== 상태 묻기 =====

    // 더 이상 진행할 게 없는 끝난 주문인지 (배송완료, 주문취소, 주문거절, 반품완료)
    // 반품신청은 관리자의 승인/거절을 기다리는 중이라 끝난 주문이 아니다
    // 화면(Thymeleaf)에서는 ${order.status.finished} 로 쓸 수 있다
    public boolean isFinished() {
        return this == DELIVERED || this == CANCELED || this == REJECTED || this == RETURNED;
    }

    // 배송이 진행 중인 단계인지 : 주문접수, 입고, 출고, 배송중 (관리자가 '다음 단계'로 넘길 수 있다)
    // 화면에서는 ${order.status.inProgress} 로 쓸 수 있다
    public boolean isInProgress() {
        return this == ORDER_RECEIVED || this == WAREHOUSED || this == SHIPPED || this == IN_DELIVERY;
    }

    // 취소, 거절, 반품되어 판매가 없던 일이 된 주문인지 (환불 대상, 판매량/매출에서 뺀다)
    // 화면에서는 ${order.status.voided} 로 쓸 수 있다
    public boolean isVoided() {
        return this == CANCELED || this == REJECTED || this == RETURNED;
    }

    // 손님이 취소할 수 있는 단계인지 : 아직 배송완료되기 전(주문접수, 입고, 출고, 배송중)
    // 화면에서는 ${order.status.cancelable} 로 쓸 수 있다
    public boolean isCancelable() {
        return isInProgress();
    }

    // 관리자가 거절할 수 있는 단계인지 : 아직 출고되기 전(주문접수, 입고)
    // 화면에서는 ${order.status.rejectable} 로 쓸 수 있다
    public boolean isRejectable() {
        return this == ORDER_RECEIVED || this == WAREHOUSED;
    }
}
