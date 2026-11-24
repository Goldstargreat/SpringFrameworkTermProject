package kr.ac.kopo.gnuyog.goldstarmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 보안 설정 클래스 : 접근 제한(로그인/관리자 인터셉터 등록), 비밀번호 암호화 도구 등록
// 어떤 주소에 누가 들어갈 수 있는지는 이 파일만 보면 알 수 있다
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    // 로그인해야 들어갈 수 있는 주소
    private static final String[] LOGIN_REQUIRED_PATHS = {
            "/cart/**", "/orders/**", "/members/mypage/**", "/admin/**",
            "/reviews/**", "/inquiries/**",  // 상품평, 상품문의 쓰기/삭제
            "/notifications/**"               // 내 알림
    };

    // 관리자(ADMIN)만 들어갈 수 있는 주소
    private static final String[] ADMIN_ONLY_PATHS = {
            "/admin/**"
    };

    // 일반 회원(손님)만 들어갈 수 있는 주소 (관리자는 물건을 사지 않으므로 장바구니/주문내역이 필요 없다)
    private static final String[] CUSTOMER_ONLY_PATHS = {
            "/cart/**", "/orders/**", "/notifications/**"
    };

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;
    private final CustomerOnlyInterceptor customerOnlyInterceptor;

    public SecurityConfig(LoginCheckInterceptor loginCheckInterceptor, AdminCheckInterceptor adminCheckInterceptor,
                          CustomerOnlyInterceptor customerOnlyInterceptor) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.customerOnlyInterceptor = customerOnlyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1) 로그인 검사 (먼저 실행)
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns(LOGIN_REQUIRED_PATHS);

        // 2) 관리자 권한 검사 (로그인 검사를 통과한 뒤 실행)
        registry.addInterceptor(adminCheckInterceptor)
                .order(2)
                .addPathPatterns(ADMIN_ONLY_PATHS);

        // 3) 손님 전용 검사 : 관리자는 장바구니/주문 화면에 들어갈 수 없다
        registry.addInterceptor(customerOnlyInterceptor)
                .order(3)
                .addPathPatterns(CUSTOMER_ONLY_PATHS);
    }

    // 비밀번호 암호화 도구를 스프링 빈으로 등록한다 (MemberService, DataInitializer 에서 주입받아 사용)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
