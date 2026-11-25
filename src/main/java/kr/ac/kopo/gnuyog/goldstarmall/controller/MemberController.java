package kr.ac.kopo.gnuyog.goldstarmall.controller;

import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.gnuyog.goldstarmall.config.SessionConst;
import kr.ac.kopo.gnuyog.goldstarmall.domain.Member;
import kr.ac.kopo.gnuyog.goldstarmall.domain.MemberGrade;
import kr.ac.kopo.gnuyog.goldstarmall.exception.BusinessException;
import kr.ac.kopo.gnuyog.goldstarmall.service.GradeService;
import kr.ac.kopo.gnuyog.goldstarmall.service.MemberService;
import kr.ac.kopo.gnuyog.goldstarmall.validator.MemberValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

// 회원 컨트롤러 : 회원가입, 아이디 중복 확인, 로그인/로그아웃, 아이디·비밀번호 찾기,
//                마이페이지(정보 수정, 비밀번호 변경, 탈퇴)
// 로그인한 회원 번호는 세션(SessionConst.LOGIN_MEMBER_ID)에 들어 있다
@Controller
@RequestMapping("/members")
public class MemberController {

    // 처리가 끝나면 돌아갈 화면
    private static final String REDIRECT_LOGIN = "redirect:/members/login";
    private static final String REDIRECT_MYPAGE = "redirect:/members/mypage";
    private static final String REDIRECT_FIND_PASSWORD = "redirect:/members/find-password";

    private final MemberService memberService;
    private final GradeService gradeService;

    public MemberController(MemberService memberService, GradeService gradeService) {
        this.memberService = memberService;
        this.gradeService = gradeService;
    }

    // ===== 회원가입 =====

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
                       @RequestParam("phone") String phone,
                       @RequestParam(name = "address", required = false) String address,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        // 입력값 검사(MemberValidator) 후 가입 : 문제가 있으면 오류 메시지를 담아 가입 화면을 다시 보여 준다
        try {
            MemberValidator.validateLoginId(loginId);
            MemberValidator.checkReservedLoginId(loginId);
            MemberValidator.validatePassword(password, passwordConfirm, loginId);
            MemberValidator.validateName(name);
            MemberValidator.checkReservedName(name);
            memberService.join(loginId.trim(), password, name.trim(), phone, address);
        } catch (BusinessException e) {
            // 입력값 오류, 아이디 중복 등
            model.addAttribute("error", e.getMessage());
            // 다시 입력하지 않도록 기존 입력값을 화면에 돌려준다 (비밀번호는 제외)
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("phone", phone);
            model.addAttribute("address", address);
            return "member/join";
        }

        // addFlashAttribute : 리다이렉트된 다음 화면에서 딱 한 번만 보이는 메시지
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
        return REDIRECT_LOGIN;
    }

    // 아이디 중복 확인 : 회원가입 화면의 "중복 확인" 버튼이 자바스크립트(fetch)로 부른다
    // @ResponseBody : 화면(HTML) 대신 데이터를 돌려준다. Map 은 JSON 으로 바뀌어 전달된다
    // 예) {"available": true, "message": "사용할 수 있는 아이디입니다."}
    // 화면을 새로고침하지 않고 결과만 받아 보여 주기 위한 것이고, 가입할 때 서버에서 한 번 더 검사한다
    @GetMapping("/check-login-id")
    @ResponseBody
    public Map<String, Object> checkLoginId(@RequestParam(name = "loginId", required = false) String loginId) {
        // 1) 글자 수 등 아이디 규칙 검사
        try {
            MemberValidator.validateLoginId(loginId);
            MemberValidator.checkReservedLoginId(loginId);
        } catch (BusinessException e) {
            return checkResult(false, e.getMessage());
        }
        // 2) 이미 쓰는 아이디인지 검사
        if (!memberService.isLoginIdAvailable(loginId.trim())) {
            return checkResult(false, "이미 사용 중인 아이디입니다.");
        }
        return checkResult(true, "사용할 수 있는 아이디입니다.");
    }

    // 아이디 중복 확인 결과 만들기
    private Map<String, Object> checkResult(boolean available, String message) {
        return Map.of("available", available, "message", message);
    }

    // ===== 로그인 / 로그아웃 =====

    // 로그인 화면 보여 주기
    // 로그인이 필요한 화면에서 쫓겨 오면 주소에 needLogin 이 붙어 있다
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

    // 로그아웃 처리 : 세션을 통째로 없앤다
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ===== 아이디 찾기 =====

    // 아이디 찾기 화면
    @GetMapping("/find-id")
    public String findIdForm() {
        return "member/find-id";
    }

    // 아이디 찾기 처리 : 이름 + 전화번호가 맞으면 가린 아이디를 같은 화면에 보여 준다
    @PostMapping("/find-id")
    public String findId(@RequestParam("name") String name,
                         @RequestParam("phone") String phone,
                         Model model) {
        try {
            model.addAttribute("foundLoginId", memberService.findLoginId(name, phone));
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            // 다시 입력하지 않도록 입력값을 돌려준다
            model.addAttribute("name", name);
            model.addAttribute("phone", phone);
        }
        return "member/find-id";
    }

    // ===== 비밀번호 찾기 =====

    // 비밀번호 찾기 화면 (1단계 : 본인 확인)
    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "member/find-password";
    }

    // 본인 확인 처리 : 통과하면 세션에 회원 번호를 기억해 두고 새 비밀번호 화면으로 보낸다
    // (주소창에 회원 번호를 넣는 방식이면 남의 비밀번호를 바꿀 수 있으므로 세션에 둔다)
    @PostMapping("/find-password")
    public String findPassword(@RequestParam("loginId") String loginId,
                               @RequestParam("name") String name,
                               @RequestParam("phone") String phone,
                               HttpSession session,
                               Model model) {
        try {
            Long memberId = memberService.verifyForPasswordReset(loginId, name, phone);
            session.setAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID, memberId);
            return "redirect:/members/reset-password";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            // 다시 입력하지 않도록 입력값을 돌려준다
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("phone", phone);
            return "member/find-password";
        }
    }

    // 새 비밀번호 화면 (2단계) : 본인 확인을 거치지 않고 바로 들어오면 1단계로 돌려보낸다
    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID) == null) {
            redirectAttributes.addFlashAttribute("error", "본인 확인을 먼저 해 주세요.");
            return REDIRECT_FIND_PASSWORD;
        }
        return "member/reset-password";
    }

    // 새 비밀번호 저장 : 성공하면 세션의 회원 번호를 지우고 로그인 화면으로 보낸다
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("newPassword") String newPassword,
                                @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "본인 확인을 먼저 해 주세요.");
            return REDIRECT_FIND_PASSWORD;
        }
        try {
            memberService.resetPassword(memberId, newPassword, newPasswordConfirm);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "member/reset-password";
        }
        // 한 번 바꾸면 끝 : 같은 세션으로 다시 바꿀 수 없게 지운다
        session.removeAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID);
        redirectAttributes.addFlashAttribute("message", "비밀번호를 변경했습니다. 새 비밀번호로 로그인해 주세요.");
        return REDIRECT_LOGIN;
    }

    // ===== 마이페이지 (SecurityConfig 에서 로그인한 사람만 들어올 수 있게 막아 두었다) =====

    // 마이페이지 화면 : 내 정보 수정, 비밀번호 변경, 회원 탈퇴
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        Member member = memberService.findById(loginMemberId(session));
        model.addAttribute("member", member);
        // 내 등급 : 누적 구매액과 다음 등급까지 남은 금액
        model.addAttribute("totalPurchase", gradeService.totalPurchase(member));
        model.addAttribute("remainingToNext", gradeService.remainingToNextGrade(member));
        model.addAttribute("grades", MemberGrade.values());
        return "member/mypage";
    }

    // 내 정보 수정 처리
    @PostMapping("/mypage/edit")
    public String edit(@RequestParam("name") String name,
                       @RequestParam("phone") String phone,
                       @RequestParam(name = "address", required = false) String address,
                       @RequestParam(name = "secondAddress", required = false) String secondAddress,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            memberService.updateInfo(loginMemberId(session), name, phone, address, secondAddress);
            // 화면 상단의 "OOO님" 도 바뀐 이름으로 보이도록 세션 값도 바꾼다
            session.setAttribute(SessionConst.LOGIN_MEMBER_NAME, name.trim());
            redirectAttributes.addFlashAttribute("message", "회원정보를 수정했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_MYPAGE;
    }

    // 비밀번호 변경 처리
    @PostMapping("/mypage/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("newPasswordConfirm") String newPasswordConfirm,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            memberService.changePassword(loginMemberId(session), currentPassword, newPassword, newPasswordConfirm);
            redirectAttributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_MYPAGE;
    }

    // 회원 탈퇴 처리 : 비밀번호가 맞으면 탈퇴하고 로그아웃한다
    @PostMapping("/mypage/delete")
    public String delete(@RequestParam("password") String password,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            memberService.withdraw(loginMemberId(session), password);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return REDIRECT_MYPAGE;
        }
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.");
        return "redirect:/";
    }

    // ===== 도우미 =====

    // 로그인한 회원 번호 (로그인할 때 세션에 저장해 둔 값)
    private Long loginMemberId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_MEMBER_ID);
    }
}
