package kr.ac.kopo.gnuyog.goldstarmall.domain;

// 회원 등급 : 배송완료된 주문의 누적 결제 금액으로 정해지고, 등급마다 주문 할인율이 다르다
// 기준 금액이나 할인율을 바꾸고 싶으면 이 파일의 숫자만 고치면 된다
public enum MemberGrade {

    NORMAL("일반", 0, 0),
    SILVER("실버", 100_000, 3),
    GOLD("골드", 300_000, 5),
    VIP("VIP", 1_000_000, 10);

    // 화면에 보여 줄 이름
    private final String label;

    // 이 등급이 되기 위한 누적 구매액 (원 이상)
    private final long minAmount;

    // 주문 할인율 (%)
    private final int discountRate;

    MemberGrade(String label, long minAmount, int discountRate) {
        this.label = label;
        this.minAmount = minAmount;
        this.discountRate = discountRate;
    }

    public String getLabel() {
        return label;
    }

    public long getMinAmount() {
        return minAmount;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    // 누적 구매액에 맞는 등급 : 위 등급부터 차례로 보고, 기준 금액 이상인 첫 등급
    public static MemberGrade fromAmount(long amount) {
        MemberGrade[] grades = values();
        for (int i = grades.length - 1; i >= 0; i--) {
            if (amount >= grades[i].minAmount) {
                return grades[i];
            }
        }
        return NORMAL;
    }

    // 바로 위 등급 (가장 높은 등급이면 null). 화면에서 ${grade.next} 로 쓴다
    public MemberGrade getNext() {
        int nextIndex = ordinal() + 1;
        if (nextIndex >= values().length) {
            return null;
        }
        return values()[nextIndex];
    }

    // 금액에 이 등급의 할인율을 적용한 할인 금액 (원 단위 아래는 버린다)
    // 예) 골드(5%) 회원이 18,780원 주문 → 939원 할인
    public int discountOf(int price) {
        return price * discountRate / 100;
    }
}
