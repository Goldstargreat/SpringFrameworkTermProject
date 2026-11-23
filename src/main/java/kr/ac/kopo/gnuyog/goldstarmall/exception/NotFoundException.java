package kr.ac.kopo.gnuyog.goldstarmall.exception;

// 찾는 데이터(회원, 상품, 주문, 장바구니 항목 등)가 없을 때 던지는 예외
// BusinessException 을 상속하므로 컨트롤러에서는 BusinessException 하나만 잡으면 함께 처리된다
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }
}
