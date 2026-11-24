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

    // 전화번호 정리 + 검사
    // 하이픈 유무와 상관없이 같은 번호로 보도록 숫자만 뽑아 "010-1234-5678" 형식으로 통일한다
    // (형식이 다르면 같은 번호인데도 중복 검사를 통과해 버리기 때문)
    // 전화번호는 필수 입력이므로 비어 있으면 예외
    public static String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("전화번호를 입력해 주세요.");
        }
        String digits = phone.replaceAll("[^0-9]", "");

        // 앞자리(국번) 길이 : 서울(02)만 2자리, 나머지(010, 031, 070 등)는 3자리
        // - 서울 : 9자리(02-123-4567) 또는 10자리(02-1234-5678)
        // - 그 외 : 10자리(031-123-4567) 또는 11자리(010-1234-5678)
        boolean seoul = digits.startsWith("02");
        boolean validLength = seoul ? digits.matches("02\\d{7,8}") : digits.matches("0[1-9]\\d{8,9}");
        if (!validLength) {
            throw new BusinessException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678, 02-123-4567)");
        }

        // 앞자리 - 가운데 - 마지막 4자리 로 나눈다
        int prefixEnd = seoul ? 2 : 3;
        int middleEnd = digits.length() - 4;
        return digits.substring(0, prefixEnd) + "-" + digits.substring(prefixEnd, middleEnd) + "-" + digits.substring(middleEnd);
    }
}
