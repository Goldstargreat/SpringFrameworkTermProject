package kr.ac.kopo.gnuyog.goldstarmall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 로그인 검사 인터셉터
// 컨트롤러가 실행되기 "전에" 먼저 실행되어, 로그인하지 않은 사용자를 로그인 화면으로 보낸다
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // false : 세션이 없으면 새로 만들지 않고 null 을 돌려준다
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER_ID) == null) {
            // 로그인하지 않았으면 로그인 화면으로 이동시키고, 컨트롤러는 실행하지 않는다
            response.sendRedirect("/members/login?needLogin=true");
            return false;
        }

        // true 를 돌려주면 원래 가려던 컨트롤러가 실행된다
        return true;
    }
}
