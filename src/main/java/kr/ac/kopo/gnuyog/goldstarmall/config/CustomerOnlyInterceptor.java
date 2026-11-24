package kr.ac.kopo.gnuyog.goldstarmall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 손님 전용 인터셉터 : 장바구니(/cart), 주문(/orders)은 일반 회원만 쓴다
// 관리자가 주소를 직접 입력해 들어오면 주문관리 화면으로 보낸다
@Component
public class CustomerOnlyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);

        String role = null;
        if (session != null) {
            role = (String) session.getAttribute(SessionConst.LOGIN_MEMBER_ROLE);
        }

        if ("ADMIN".equals(role)) {
            response.sendRedirect("/admin/orders");
            return false;
        }
        return true;
    }
}
