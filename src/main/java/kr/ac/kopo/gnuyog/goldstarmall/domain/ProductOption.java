package kr.ac.kopo.gnuyog.goldstarmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 상품 옵션 엔티티 (예: 노트북 백팩의 "15인치 / 화이트", "17인치 / 블랙")
// 옵션이 있는 상품은 옵션마다 재고를 따로 관리하고, 상품의 재고(Product.stock)는 옵션 재고의 합계로 맞춘다
// 옵션은 resources/sql/products.sql 로 넣는다
@Entity
@Table(name = "product_options")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품 1개 : 옵션 N개
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 옵션 이름 (화면에 그대로 보여 준다)
    @Column(nullable = false, length = 50)
    private String name;

    // 이 옵션의 재고 수량
    @Column(nullable = false)
    private int stock;

    public ProductOption() {
    }

    // 품절 임박인지 (재고 1 ~ 2개, 기준은 상품과 같다)
    public boolean isLowStock() {
        return stock > 0 && stock <= Product.LOW_STOCK_LIMIT;
    }

    // 재고가 부족할 때 손님에게 보여 줄 문구 : 품절 임박일 때만 남은 개수를 알려 준다
    public String getStockShortageMessage() {
        if (isLowStock()) {
            return "'" + name + "' 옵션의 재고가 부족합니다. (남은 수량 " + stock + "개)";
        }
        if (stock == 0) {
            return "'" + name + "' 옵션은 일시 품절입니다.";
        }
        return "'" + name + "' 옵션의 재고가 부족합니다. 주문 수량을 줄여 주세요.";
    }

    // ===== getter / setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
