package Lim.boardApp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException{

    private ExceptionInfo exceptionInfo;
}
