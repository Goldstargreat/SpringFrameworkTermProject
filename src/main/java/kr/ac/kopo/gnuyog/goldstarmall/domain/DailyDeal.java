package kr.ac.kopo.gnuyog.goldstarmall.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

// 오늘의 할인 : 요일마다 한 카테고리를 할인한다
// - 할인가는 상품 가격에서 할인율만큼 빼고 10원 단위는 버린다 (Product.getSalePrice)
// - 장바구니, 바로 주문, 주문 금액 모두 할인가로 계산하고, 회원 등급 할인은 할인가에 한 번 더 적용된다
// - 주문할 때의 할인가가 주문 상품(OrderItem.orderPrice)에 저장되므로, 요일이 바뀌어도 주문 금액은 그대로다
public enum DailyDeal {

    MONDAY(DayOfWeek.MONDAY, "월", "식품", 10),
    TUESDAY(DayOfWeek.TUESDAY, "화", "생활용품", 10),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "수", "패션의류", 15),
    THURSDAY(DayOfWeek.THURSDAY, "목", "가전디지털", 5),
    FRIDAY(DayOfWeek.FRIDAY, "금", "뷰티", 15),
    SATURDAY(DayOfWeek.SATURDAY, "토", "스포츠/레저", 10),
    SUNDAY(DayOfWeek.SUNDAY, "일", "완구/취미", 10);

    // 쇼핑몰 기준 시간대 (서버가 다른 나라에 있어도 한국 날짜로 요일을 정한다)
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final DayOfWeek dayOfWeek;
    private final String dayLabel;     // 화면에 보여 줄 요일 (월, 화, ...)
    private final String category;     // 할인하는 카테고리 (ProductService.CATEGORIES 의 이름과 같아야 한다)
    private final int rate;            // 할인율 (%)

    DailyDeal(DayOfWeek dayOfWeek, String dayLabel, String category, int rate) {
        this.dayOfWeek = dayOfWeek;
        this.dayLabel = dayLabel;
        this.category = category;
        this.rate = rate;
    }

    // 오늘의 할인 (한국 날짜 기준)
    public static DailyDeal today() {
        DayOfWeek today = LocalDate.now(SEOUL).getDayOfWeek();
        for (DailyDeal deal : values()) {
            if (deal.dayOfWeek == today) {
                return deal;
            }
        }
        throw new IllegalStateException("요일별 할인이 빠짐없이 정해져 있어야 합니다.");
    }

    // 이 카테고리가 이 할인의 대상인지
    public boolean appliesTo(String productCategory) {
        return category.equals(productCategory);
    }

    // 할인가 : 할인율만큼 빼고 10원 단위는 버린다 (예: 6,480원 10% → 5,832원 → 5,830원)
    public int discountedPrice(int price) {
        int discounted = price * (100 - rate) / 100;
        return discounted / 10 * 10;
    }

    // 오늘인지 (주간 할인표에서 오늘 칸을 강조할 때 쓴다, 화면에서 deal.today 로 사용)
    public boolean isToday() {
        return this == today();
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public String getCategory() {
        return category;
    }

    public int getRate() {
        return rate;
    }
}
