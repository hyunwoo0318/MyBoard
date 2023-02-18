package Lim.boardApp.form;

import Lim.boardApp.domain.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomerRegisterFormTest {

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
    @DisplayName("정상적인 회원가입")
    public void regCustomerNormal(){
        CustomerRegisterForm customerRegisterForm = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hyunwoo", 20);
        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(customerRegisterForm);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("아이디를 입력하지 않은경우")
    public void regCustomerLoginIdNull(){
        CustomerRegisterForm formNullLoginId = new CustomerRegisterForm("","pw123123","pw123123","hyunwoo",20);

        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(formNullLoginId);

        assertThat(result.size()).isEqualTo(1);
        for (ConstraintViolation<CustomerRegisterForm> v : result) {
            assertThat(v.getMessageTemplate()).isEqualTo("아이디를 입력해주세요.");
        }
    }

    @Test
    @DisplayName("비밀번호를 입력하지 않은경우")
    public void regCustomerPasswordNull(){
        CustomerRegisterForm formNullPassword = new CustomerRegisterForm("id123123","","pw123123","hyunwoo",20);

        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(formNullPassword);

        assertThat(result.size()).isEqualTo(1);
        for (ConstraintViolation<CustomerRegisterForm> v : result) {
            assertThat(v.getMessageTemplate()).isEqualTo("비밀번호를 입력해주세요.");
        }
    }

    @Test
    @DisplayName("비밀번호 확인창에 입력하지 않은경우")
    public void regCustomerPasswordCheckNull(){

        CustomerRegisterForm formNullPasswordCheck = new CustomerRegisterForm("id123123", "pw123213", "", "hyunwoo", 23);

        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(formNullPasswordCheck);

        assertThat(result.size()).isEqualTo(1);
        for (ConstraintViolation<CustomerRegisterForm> c : result) {
            assertThat(c.getMessageTemplate()).isEqualTo("비밀번호 확인창을 입력해주세요");
        }
    }

    @Test
    @DisplayName("이름을 입력하지 않은경우")
    public void regCustomerNameNull() {

        CustomerRegisterForm formNullName = new CustomerRegisterForm("id123123", "pw123123", "pw123123", "", 20);

        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(formNullName);

        assertThat(result.size()).isEqualTo(1);
        for (ConstraintViolation<CustomerRegisterForm> v : result) {
            assertThat(v.getMessageTemplate()).isEqualTo("이름을 입력해주세요.");
        }
    }

    @Test
    @DisplayName("이름을 20자 이상으로 입력한경우")
    public void regCustomerNameOver20(){
        CustomerRegisterForm formNameOver20 = new CustomerRegisterForm("id123123","pw123123","pw123123",
                "name123123123123123123123123123123",20);

        Set<ConstraintViolation<CustomerRegisterForm>> result = validator.validate(formNameOver20);

        assertThat(result.size()).isEqualTo(1);
        for (ConstraintViolation<CustomerRegisterForm> v : result) {
            assertThat(v.getMessageTemplate()).isEqualTo("이름의 최대길이는 20입니다.");
        }
    }
    //TODO : 아이디 중복 조건 확인 테스트
}