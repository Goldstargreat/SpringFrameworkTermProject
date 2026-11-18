package kr.ac.kopo.gnuyog.goldstarmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 웹 설정 클래스 : 인터셉터 등록, 비밀번호 암호화 도구 등록
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;

    public WebConfig(LoginCheckInterceptor loginCheckInterceptor, AdminCheckInterceptor adminCheckInterceptor) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 장바구니, 주문, 관리자 화면은 로그인해야 들어갈 수 있다
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/cart/**", "/orders/**", "/admin/**");

        // 관리자 화면은 로그인 + 관리자 권한까지 있어야 한다
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin/**");
    }

    // 비밀번호 암호화 도구를 스프링 빈으로 등록한다 (MemberService 에서 주입받아 사용)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
