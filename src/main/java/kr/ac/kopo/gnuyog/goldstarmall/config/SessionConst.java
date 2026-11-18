package kr.ac.kopo.gnuyog.goldstarmall.config;

// 세션에 값을 넣고 꺼낼 때 쓰는 이름(key)을 한곳에 모아 둔 클래스
// 문자열을 직접 쓰다가 오타가 나는 것을 막기 위해 상수로 만든다
public class SessionConst {

    // 로그인한 회원의 번호(PK)
    public static final String LOGIN_MEMBER_ID = "loginMemberId";

    // 로그인한 회원의 이름 (화면 상단에 "OOO님" 표시용)
    public static final String LOGIN_MEMBER_NAME = "loginMemberName";

    // 로그인한 회원의 권한 ("USER" 또는 "ADMIN")
    public static final String LOGIN_MEMBER_ROLE = "loginMemberRole";
}
