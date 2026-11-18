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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false, length = 100)
    private String name;

    // 카테고리 (식품, 생활용품, 가전, 패션)
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

    public Product() {
    }

    // 초기 데이터를 넣을 때 편하게 쓰는 생성자
    public Product(String name, String category, int price, int stock, String description, String icon) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.icon = icon;
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
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
}
