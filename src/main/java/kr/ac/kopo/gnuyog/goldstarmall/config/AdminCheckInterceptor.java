package kr.ac.kopo.gnuyog.goldstarmall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 관리자 검사 인터셉터 : /admin 으로 시작하는 주소는 관리자(ADMIN)만 들어갈 수 있다
@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);

        String role = null;
        if (session != null) {
            role = (String) session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE);
        }

        if (!Member.ROLE_ADMIN.equals(role)) {
            // 관리자가 아니면 메인 화면으로 돌려보낸다
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}
