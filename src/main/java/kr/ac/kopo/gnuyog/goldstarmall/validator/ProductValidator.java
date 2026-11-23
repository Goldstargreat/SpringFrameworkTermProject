package kr.ac.kopo.gnuyog.goldstarmall.validator;

import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;

import java.util.List;

// 상품 입력값 검사기 : 관리자가 상품을 등록하거나 가격/재고를 수정할 때 쓰는 규칙을 모아 둔다
// 검사에 실패하면 BusinessException 을 던지고, 컨트롤러가 메시지를 화면에 보여 준다
public final class ProductValidator {

    // DB 컬럼 길이보다 길면 저장할 때 오류가 나므로 미리 막는다 (Product 의 @Column length 참고)
    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int ICON_MAX_LENGTH = 10;

    // 객체를 만들 필요가 없는 클래스이므로 생성자를 막아 둔다
    private ProductValidator() {
    }

    // 새 상품 등록 검사 : 상품명, 카테고리, 가격/재고, 글자 수
    public static void validateNewProduct(String name, String category, List<String> categories,
                                          int price, int stock, String description, String icon) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("상품명을 입력해 주세요.");
        }
        if (!categories.contains(category)) {
            throw new BusinessException("카테고리를 선택해 주세요.");
        }
        validatePriceAndStock(price, stock);
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new BusinessException("상품명은 " + NAME_MAX_LENGTH + "글자까지 입력할 수 있습니다.");
        }
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException("상품 설명은 " + DESCRIPTION_MAX_LENGTH + "글자까지 입력할 수 있습니다.");
        }
        if (icon != null && icon.trim().length() > ICON_MAX_LENGTH) {
            throw new BusinessException("이모지는 한두 개만 입력해 주세요.");
        }
    }

    // 가격은 1원 이상, 재고는 0개 이상이어야 한다
    public static void validatePriceAndStock(int price, int stock) {
        if (price < 1) {
            throw new BusinessException("가격은 1원 이상이어야 합니다.");
        }
        if (stock < 0) {
            throw new BusinessException("재고는 0개 이상이어야 합니다.");
        }
    }
}
