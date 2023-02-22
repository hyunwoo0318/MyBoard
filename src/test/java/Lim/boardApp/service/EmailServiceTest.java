package Lim.boardApp.service;

import Lim.boardApp.repository.CustomerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class EmailServiceTest {

    @Autowired EmailService emailService;
    @Autowired
    CustomerRepository customerRepository;

    @Test
    @DisplayName("정확한 이메일 형식 입력시 판별")
    public void checkEmailFormSuccess(){
        String email1 = "hyunwoo0318@naver.com";
        String email2 = "ex@ex.com";

        boolean result1 = emailService.checkEmailForm(email1);
        boolean result2 = emailService.checkEmailForm(email2);

        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
    }

    @Test
    @DisplayName("잘못된 이메일 형식 입력시 판별")
    public void checkEmailFormFail(){
        String email1 = "";
        String email2 = "hyunwoo0318naver.com";

        boolean result1 = emailService.checkEmailForm(email1);
        boolean result2 = emailService.checkEmailForm(email2);

        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }
}