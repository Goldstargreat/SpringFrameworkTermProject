package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public Product() {
    }

    // ===== getter / setter =====
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
