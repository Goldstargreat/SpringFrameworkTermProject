package kr.ac.kopo.gnuyog.goldstarmall.validator;

import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;

import java.util.List;

// 회원 입력값 검사기 : 회원 입력값 규칙(글자 수 등)과 검사 메서드를 한곳에 모아 둔다
// 규칙을 바꾸고 싶으면 이 파일의 상수만 고치면 회원가입, 비밀번호 변경 등에 모두 적용된다
// 검사에 실패하면 BusinessException 을 던지고, 컨트롤러가 메시지를 화면에 보여 준다
public final class MemberValidator {

    // 아이디 규칙 : 영문 소문자만, 4 ~ 20글자 (숫자, 한글, 대문자, 공백, 특수문자는 안 된다)
    public static final int LOGIN_ID_MIN_LENGTH = 4;
    public static final int LOGIN_ID_MAX_LENGTH = 20;

    // 비밀번호 규칙 : 8 ~ 20글자, 영문 + 숫자 + 특수문자를 모두 포함, 공백 안 됨
    // 추가로 아이디가 들어간 비밀번호, 같은 글자를 4번 이상 연속으로 쓴 비밀번호(aaaa, 1111)는 막는다
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;

    // 일반 회원이 아이디에 쓸 수 없는 단어 (관리자나 쇼핑몰로 착각하지 않도록)
    // 대소문자, 띄어쓰기와 상관없이 들어 있기만 해도 막는다 (예: Admin1, myadmin, GoldStarMall)
    private static final List<String> RESERVED_LOGIN_ID_WORDS = List.of("admin", "goldstarmall");

    // 일반 회원이 이름에 쓸 수 없는 단어 (상품평/문의에 관리자처럼 보이지 않도록)
    private static final List<String> RESERVED_NAME_WORDS = List.of(
            "admin", "관리자", "운영자", "골드스타몰", "goldstarmall");

    // 객체를 만들 필요가 없는 클래스이므로 생성자를 막아 둔다
    private MemberValidator() {
    }

    // 아이디에 쓸 수 없는 단어가 들어 있는지 검사 (회원가입, 아이디 중복 확인)
    public static void checkReservedLoginId(String loginId) {
        if (containsReservedWord(loginId, RESERVED_LOGIN_ID_WORDS)) {
            throw new BusinessException("'admin', 'goldstarmall'이 들어간 아이디는 사용할 수 없습니다.");
        }
    }

    // 이름에 쓸 수 없는 단어가 들어 있는지 검사 (회원가입, 일반 회원의 정보 수정)
    // 관리자 계정은 원래 이름이 "관리자"이므로 이 검사를 하지 않는다
    public static void checkReservedName(String name) {
        if (containsReservedWord(name, RESERVED_NAME_WORDS)) {
            throw new BusinessException("'관리자', '운영자', 'admin', '골드스타몰', 'goldstarmall'이 들어간 이름은 사용할 수 없습니다.");
        }
    }

    // 띄어쓰기를 지우고 소문자로 바꾼 뒤, 금지 단어가 하나라도 들어 있는지
    private static boolean containsReservedWord(String text, List<String> words) {
        if (text == null) {
            return false;
        }
        String normalized = text.replaceAll("\\s", "").toLowerCase();
        for (String word : words) {
            if (normalized.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // 아이디 검사 : 영문 소문자만 4 ~ 20글자
    public static void validateLoginId(String loginId) {
        String id = loginId == null ? "" : loginId.trim();
        if (id.length() < LOGIN_ID_MIN_LENGTH || id.length() > LOGIN_ID_MAX_LENGTH) {
            throw new BusinessException("아이디는 " + LOGIN_ID_MIN_LENGTH + " ~ " + LOGIN_ID_MAX_LENGTH + "글자로 입력해 주세요.");
        }
        if (!id.matches("[a-z]+")) {
            throw new BusinessException("아이디는 영문 소문자만 쓸 수 있습니다. (숫자, 한글, 대문자, 공백, 특수문자 불가)");
        }
    }

    // 새 비밀번호 검사 : 글자 수, 영문/숫자/특수문자 포함, 공백, 아이디 포함, 같은 글자 연속, 비밀번호 확인 일치
    // loginId : 이 비밀번호를 쓸 회원의 아이디 (비밀번호에 아이디가 들어 있는지 보기 위해)
    public static void validatePassword(String password, String passwordConfirm, String loginId) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new BusinessException("비밀번호는 " + PASSWORD_MIN_LENGTH + " ~ " + PASSWORD_MAX_LENGTH + "글자로 입력해 주세요.");
        }
        if (password.matches(".*\\s.*")) {
            throw new BusinessException("비밀번호에는 공백을 쓸 수 없습니다.");
        }
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new BusinessException("비밀번호는 영문, 숫자, 특수문자(!@#$ 등)를 모두 포함해야 합니다.");
        }
        if (loginId != null && !loginId.trim().isEmpty()
                && password.toLowerCase().contains(loginId.trim().toLowerCase())) {
            throw new BusinessException("비밀번호에 아이디를 넣을 수 없습니다.");
        }
        // (.)\\1{3} : 아무 글자 하나 뒤에 같은 글자가 3번 더 = 같은 글자 4번 연속
        if (password.matches(".*(.)\\1{3}.*")) {
            throw new BusinessException("같은 글자를 4번 이상 연속으로 쓸 수 없습니다. (예: aaaa, 1111)");
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
