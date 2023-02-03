package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class CustomerServiceTest {

    @Autowired CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    private void registerNormalCustomer(){
        Customer regCustomer = Customer.builder()
                .loginId("id123123")
                .name("hyeonwoo")
                .password("pw123123")
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
        assertThat(result.getPassword()).isEqualTo(correctPassword);
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
    @DisplayName("이미 존재하는 아이디로 회원가입 시도")
    public void regCustomerExistLoginId(){
        registerNormalCustomer();
        CustomerRegisterForm dupCustomer = new CustomerRegisterForm("id123123", "pw2", "hy", 21);

        boolean result = customerService.dupLoginId(dupCustomer);

        assertThat(result).isTrue();
    }
    //TODO : 로그아웃 테스트
}