package Lim.boardApp.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 Error Handling
     * @param e
     * @return
     */
    @ExceptionHandler(value = {ClassCastException.class})
    public String handleClassCastException(ClassCastException e) {
        return "redirect:/customer-login";
    }

    @ExceptionHandler(value = {Exception.class})
    public String handleException(Exception e){
        return "redirect:/";
    }
}
