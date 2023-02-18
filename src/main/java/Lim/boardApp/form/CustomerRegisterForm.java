package Lim.boardApp.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterForm {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인창을 입력해주세요")
    private String passwordCheck;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 20, message = "이름의 최대길이는 20입니다.")
    private String name;

    @Min(value=1, message="너무 작은 수입니다.") @Max(value=150, message = "너무 큰 수입니다.")
    private Integer age;

    private Long kakaoId;

    public CustomerRegisterForm(String loginId, String password, String passwordCheck, String name, Integer age) {
        this.loginId = loginId;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.name = name;
        this.age = age;
    }
}
