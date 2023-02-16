package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import Lim.boardApp.form.CustomerRegisterForm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@SpringBootTest
@Transactional
class CustomerServiceTest {

    @Autowired CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    private void registerNormalCustomer(){
        String password = "pw123123";
        String salt = "salt123123";
        String passwordHash = customerService.hashPassword(password,salt);
        Customer regCustomer = Customer.builder()
                .loginId("id123123")
                .name("hyeonwoo")
                .password(passwordHash + salt)
                .age(26)
                .build();

        customerRepository.save(regCustomer);
    }


    @Test
    @DisplayName("정상적인 로그인")
    public void loginSuccess(){

        registerNormalCustomer();

        String correctId = "id123123";
        String correctPassword = "pw123123";

        Customer result = customerService.login(correctId, correctPassword);

        assertThat(result.getLoginId()).isEqualTo(correctId);
        assertThat(result.getAge()).isEqualTo(26);
        assertThat(result.getName()).isEqualTo("hyeonwoo");
    }

    @Test
    @DisplayName("ID혹은 비밀번호가 틀린 경우 로그인 상황")
    public void loginFail(){

        registerNormalCustomer();

        String correctId = "id123123";
        String correctPassword = "pw123123";

        String incorrectId = "id456456";
        String incorrectPassword = "pw456456";

        Customer incorrectPasswordCustomer = customerService.login(correctId, incorrectPassword);
        Customer incorrectIdCustomer = customerService.login(incorrectPassword, correctPassword);
        Customer incorrectIdAndPasswordCustomer = customerService.login(incorrectId, incorrectPassword);

        assertThat(incorrectPasswordCustomer).isNull();
        assertThat(incorrectIdCustomer).isNull();
        assertThat(incorrectIdAndPasswordCustomer).isNull();
    }

    @Test
    @DisplayName("정상적인 회원가입 시도")
    public void regCustomer(){
        registerNormalCustomer();
        CustomerRegisterForm customer = new CustomerRegisterForm("id456456", "pw123123", "john", 12);

        boolean result = customerService.dupLoginId(customer);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시도")
    public void regCustomerExistLoginId(){
        registerNormalCustomer();
        CustomerRegisterForm dupCustomer = new CustomerRegisterForm("id123123", "pw2", "hy", 21);

        boolean result = customerService.dupLoginId(dupCustomer);

        assertThat(result).isTrue();
    }
    //TODO : 로그아웃 테스트

    @Test
    @DisplayName("makeSaltTest")
    public void makeSaltTest(){
        String pw = customerService.makeSalt(20);
        System.out.println(pw);
        assertThat(pw.length()).isEqualTo(20);
    }

    @Test
    @DisplayName("hashPasswordTest")
    public void hashPasswordTest() throws NoSuchAlgorithmException {
        /**
         * expect result = SHA256(password + salt)
         */
        String password = "pw123123";
        String salt = "a123456789987654321";

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((password + salt).getBytes());
        byte[] digest = md.digest();
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            builder.append(String.format("%02X", b));
        }
        String passwordHash = builder.toString();
        System.out.println("passwordHash.length() = " + passwordHash.length());

        String result = customerService.hashPassword(password,salt);

        assertThat(result).isEqualTo(passwordHash);
    }
}

