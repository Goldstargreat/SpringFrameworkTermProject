package kr.ac.kopo.gnuyog.goldstarmall.exception;

// 업무 규칙 예외 : 재고 부족, 입력값 오류, 권한 없음처럼 "사용자에게 이유를 알려 줘야 하는" 상황에서 던진다
// 서비스/검사기(validator)가 던지고, 컨트롤러가 잡아서 메시지를 화면에 보여 준다
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
