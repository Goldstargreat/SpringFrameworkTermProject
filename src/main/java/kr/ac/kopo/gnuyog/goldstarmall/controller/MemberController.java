package kr.ac.kopo.gnuyog.goldstarmall.controller;

import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 회원 컨트롤러 : 회원가입, 로그인, 로그아웃
@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원가입 화면 보여 주기
    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(@RequestParam("loginId") String loginId,
                       @RequestParam("password") String password,
                       @RequestParam("passwordConfirm") String passwordConfirm,
                       @RequestParam("name") String name,
                       @RequestParam(name = "phone", required = false) String phone,
                       @RequestParam(name = "address", required = false) String address,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        // 입력값 검사 : 문제가 있으면 오류 메시지를 담아 가입 화면을 다시 보여 준다
        String error = null;
        if (loginId.trim().length() < 4) {
            error = "아이디는 4글자 이상이어야 합니다.";
        } else if (password.length() < 4) {
            error = "비밀번호는 4글자 이상이어야 합니다.";
        } else if (!password.equals(passwordConfirm)) {
            error = "비밀번호와 비밀번호 확인이 서로 다릅니다.";
        } else if (name.trim().isEmpty()) {
            error = "이름을 입력해 주세요.";
        }

        if (error == null) {
            try {
                memberService.join(loginId.trim(), password, name.trim(), phone, address);
            } catch (IllegalStateException e) {
                // 아이디 중복 등 서비스에서 던진 오류
                error = e.getMessage();
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
            // 다시 입력하지 않도록 기존 입력값을 화면에 돌려준다 (비밀번호는 제외)
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("phone", phone);
            model.addAttribute("address", address);
            return "member/join";
        }

        // addFlashAttribute : 리다이렉트된 다음 화면에서 딱 한 번만 보이는 메시지
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
        return "redirect:/members/login";
    }

    // 로그인 화면 보여 주기
    @GetMapping("/login")
    public String loginForm(@RequestParam(name = "needLogin", required = false) String needLogin, Model model) {
        if (needLogin != null) {
            model.addAttribute("error", "로그인이 필요한 서비스입니다.");
        }
        return "member/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("loginId") String loginId,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        Member member = memberService.login(loginId, password);
        if (member == null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            model.addAttribute("loginId", loginId);
            return "member/login";
        }

        // 로그인 성공 : 세션에 회원 정보를 저장한다 (브라우저를 닫거나 로그아웃할 때까지 유지)
        session.setAttribute(SessionConst.LOGIN_MEMBER_ID, member.getId());
        session.setAttribute(SessionConst.LOGIN_MEMBER_NAME, member.getName());
        session.setAttribute(SessionConst.LOGIN_MEMBER_ROLE, member.getRole());
        return "redirect:/";
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // 세션을 통째로 없앤다
        session.invalidate();
        return "redirect:/";
    }
}
