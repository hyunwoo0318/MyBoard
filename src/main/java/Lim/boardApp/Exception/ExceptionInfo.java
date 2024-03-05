package Lim.boardApp.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionInfo {
    NOT_FOUND("notFound", "존재하지 않는 리소스입니다."),
    DUP_LOGIN_ID("dupLoginId", "중복된 로그인 아이디입니다."),
    DUP_EMAIL("dupEmail", "이미 가입된 이메일입니다."),
    INVALID_PASSWORD_CHECK("invalidPasswordCheck", "비밀번호와 비밀번호 확인이 다릅니다."),
    LOGIN_FAIL("loginFail", "로그인에 실패했습니다."),
    EMAIL_AUTH_FAIL("emailAuthFail", "이메일 인증에 실패했습니다."),

    ;


    private final String errorCode;
    private final String message;

}


