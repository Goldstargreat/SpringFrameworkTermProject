package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 상품 엔티티 : products 테이블
@Entity
@Table(name = "products")
public class Product
{
    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false, length = 100)
    private String name;

    // 카테고리 (패션의류, 식품, 가전디지털 등 15가지. 목록은 ProductService 참고)
    @Column(nullable = false, length = 30)
    private String category;

    // 가격(원)
    @Column(nullable = false)
    private int price;

    // 재고 수량 : 주문하면 줄어들고, 주문을 취소하면 다시 늘어난다
    @Column(nullable = false)
    private int stock;

    // 상품 설명
    @Column(length = 500)
    private String description;

    // 상품 그림 대신 보여 줄 이모지 (이미지 파일 없이도 화면이 채워지도록)
    @Column(length = 10)
    private String icon;

    // 제조사/브랜드 (예: 신라면 → 농심). 상품 상세 화면의 "상품 정보" 표에 보인다
    @Column(length = 50)
    private String manufacturer;

    // 원산지 (예: 대한민국, 국내산 (제주), 중국)
    @Column(length = 50)
    private String origin;

    // 판매 집계 초기화 시각 : 관리자가 누적 판매량/매출을 초기화한 때 (한 번도 안 했으면 null)
    // 주문 기록은 지우지 않고, 이 시각 이후의 주문만 누적 판매량/매출에 센다
    private LocalDateTime salesResetAt;

    // 로켓 배송 여부 : 관리자가 상품마다 정한다 (상품관리 화면에서 켜고 끈다)
    // 비어 있으면(null) 로켓 배송이 아닌 것으로 본다
    private Boolean fastDelivery;

    // 옵션 목록 (예: 15인치 / 화이트). 옵션이 없는 상품은 비어 있다
    // 옵션이 있으면 손님은 옵션을 골라야 담거나 주문할 수 있고, 재고는 옵션마다 따로 센다
    // cascade = ALL : 상품을 지우면 옵션도 함께 지워진다
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<ProductOption> options = new ArrayList<>();

    public Product() {
    }

    // 옵션이 있는 상품인지 (화면에서 product.hasOptions 로 사용)
    public boolean isHasOptions() {
        return !options.isEmpty();
    }

    public List<ProductOption> getOptions() {
        return options;
    }

    // ===== getter / setter =====
    public LocalDateTime getSalesResetAt() {
        return salesResetAt;
    }

    public void setSalesResetAt(LocalDateTime salesResetAt) {
        this.salesResetAt = salesResetAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    // 손님에게 남은 개수를 알려 주는 기준 : 이 개수 이하로 남았을 때만 "N개 남음"을 보여 준다
    // (재고가 넉넉할 때 정확한 숫자를 보여 주면 쇼핑몰의 판매량/입고량이 그대로 드러나기 때문)
    public static final int LOW_STOCK_LIMIT = 2;

    // 로켓 배송 상품인지 (관리자가 정한 값) → 화면에서 product.fastDelivery 로 사용
    public boolean isFastDelivery() {
        return Boolean.TRUE.equals(fastDelivery);
    }

    public void setFastDelivery(boolean fastDelivery) {
        this.fastDelivery = fastDelivery;
    }

    // 손님 화면에 "로켓 배송"을 붙일지 : 관리자가 켜 둔 상품이라도 품절이면 붙이지 않는다
    public boolean isShowFastDelivery() {
        return isFastDelivery() && stock > 0;
    }

    // 품절 임박인지 (재고 1 ~ 2개) → 화면에서 product.lowStock 으로 사용
    public boolean isLowStock() {
        return stock > 0 && stock <= LOW_STOCK_LIMIT;
    }

    // 재고가 부족할 때 손님에게 보여 줄 문구 : 품절 임박일 때만 남은 개수를 알려 준다
    public String getStockShortageMessage() {
        if (isLowStock())
        {
            return "재고가 부족합니다. (남은 수량 " + stock + "개)";
        }
        return "재고가 부족합니다. 주문 수량을 줄여 주세요.";
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
