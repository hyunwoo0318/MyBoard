package Lim.boardApp.domain.customer.form;

import Lim.boardApp.common.annotation.emailvalidator.CustomEmail;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class EmailAuthForm {
    @CustomEmail
    private String email;

    @NotBlank(message = "이메일 인증번호를 입력해주세요.")
    private String emailAuth;
}
