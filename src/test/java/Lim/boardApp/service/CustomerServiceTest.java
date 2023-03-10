package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import Lim.boardApp.form.CustomerRegisterForm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    public void registerNormalCustomer(){
        String password = "pw123123";
        String salt = "salt123123";
        String passwordHash = customerService.hashPassword(password,salt);
        customer = Customer.builder()
                .loginId("id123123")
                .name("hyeonwoo")
                .password(passwordHash + salt)
                .age(26)
                .build();
    }


    @Test
    @DisplayName("정상적인 로그인")
    public void loginSuccess(){

        given(customerRepository.findByLoginId(customer.getLoginId()))
                .willReturn(Optional.of(customer));

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

        given(customerRepository.findByLoginId(customer.getLoginId()))
                .willReturn(Optional.of(customer));

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

        given(customerRepository.findByLoginId("id456456"))
                .willReturn(Optional.empty());

        CustomerRegisterForm customerForm = new CustomerRegisterForm("id456456", "pw123123","pw123123", "john", 12);

        boolean result = customerService.dupLoginId(customerForm);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시도")
    public void regCustomerExistLoginId(){
        given(customerRepository.findByLoginId(customer.getLoginId()))
                .willReturn(Optional.of(customer));

        CustomerRegisterForm dupCustomer = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hy", 21);

        boolean result = customerService.dupLoginId(dupCustomer);

        assertThat(result).isTrue();
    }

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

