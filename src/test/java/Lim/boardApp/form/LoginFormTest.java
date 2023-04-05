package Lim.boardApp.form;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class LoginFormTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void init(){
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void clear(){
        validatorFactory.close();
    }

    @Test
    @DisplayName("정상적인 로그인과정")
    public void normalLogin(){

        LoginForm loginForm = new LoginForm("id123123", "pw123123");
        Set<ConstraintViolation<LoginForm>> result = validator.validate(loginForm);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("아이디나 비밀번호를 입력하지 않은경우")
    public void missingLogin(){
        LoginForm noPwLoginForm = new LoginForm("id123123", "");
        LoginForm noIdLoginForm = new LoginForm("", "pw123123");

        Set<ConstraintViolation<LoginForm>> result1 = validator.validate(noPwLoginForm);
        Set<ConstraintViolation<LoginForm>> result2 = validator.validate(noIdLoginForm);

        assertThat(result1.size()).isEqualTo(1);
        assertThat(result2.size()).isEqualTo(1);

        for (ConstraintViolation<LoginForm> c : result1) {
            assertThat(c.getMessageTemplate()).isEqualTo("비밀번호를 입력해주세요");
        }
        for (ConstraintViolation<LoginForm> c : result2) {
            assertThat(c.getMessageTemplate()).isEqualTo("아이디를 입력해주세요");
        }
    }
}