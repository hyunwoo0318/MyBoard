package Lim.boardApp.exception;

import org.springframework.validation.BindingResult;

public class BindingResultHelper {

    public static void setBindingResult(CustomException customException, BindingResult bindingResult){
        ExceptionInfo exceptionInfo = customException.getExceptionInfo();
        bindingResult.reject(exceptionInfo.getErrorCode(), exceptionInfo.getMessage());
    }
}
