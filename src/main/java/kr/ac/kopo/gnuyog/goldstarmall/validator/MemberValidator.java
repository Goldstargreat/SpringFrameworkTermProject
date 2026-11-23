package kr.ac.kopo.gnuyog.goldstarmall.validator;

import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;

// 회원 입력값 검사기 : 회원 입력값 규칙(글자 수 등)과 검사 메서드를 한곳에 모아 둔다
// 규칙을 바꾸고 싶으면 이 파일의 상수만 고치면 회원가입, 비밀번호 변경 등에 모두 적용된다
// 검사에 실패하면 BusinessException 을 던지고, 컨트롤러가 메시지를 화면에 보여 준다
public final class MemberValidator {

    // 아이디 최소 글자 수
    public static final int LOGIN_ID_MIN_LENGTH = 4;

    // 비밀번호 최소 글자 수
    public static final int PASSWORD_MIN_LENGTH = 4;

    // 객체를 만들 필요가 없는 클래스이므로 생성자를 막아 둔다
    private MemberValidator() {
    }

    // 아이디 검사
    public static void validateLoginId(String loginId) {
        if (loginId == null || loginId.trim().length() < LOGIN_ID_MIN_LENGTH) {
            throw new BusinessException("아이디는 " + LOGIN_ID_MIN_LENGTH + "글자 이상이어야 합니다.");
        }
    }

    // 새 비밀번호 검사 : 글자 수 + 비밀번호 확인과 일치하는지
    public static void validatePassword(String password, String passwordConfirm) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new BusinessException("비밀번호는 " + PASSWORD_MIN_LENGTH + "글자 이상이어야 합니다.");
        }
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException("비밀번호와 비밀번호 확인이 서로 다릅니다.");
        }
    }

    // 이름 검사
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("이름을 입력해 주세요.");
        }
    }
}
