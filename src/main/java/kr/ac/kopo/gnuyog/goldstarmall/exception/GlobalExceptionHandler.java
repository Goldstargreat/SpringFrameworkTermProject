package kr.ac.kopo.gnuyog.goldstarmall.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 전역 예외 처리기 : 컨트롤러에서 잡지 못한 BusinessException 을 마지막으로 받아 준다
// 각 컨트롤러는 화면에 맞게 직접 try-catch 로 처리하고, 혹시 빠뜨린 곳이 있으면
// 오류 페이지(500) 대신 메인 화면으로 돌아가서 메시지를 보여 준다
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }
}
