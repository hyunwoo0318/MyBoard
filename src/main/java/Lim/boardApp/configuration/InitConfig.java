package Lim.boardApp.configuration;

import Lim.boardApp.ObjectValue.RoleConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.repository.CustomerRepository;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitConfig {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initCustomer(){
        Customer testCustomer = Customer.builder()
            .age(123)
            .email("hyunwoo0318@naver.com")
            .role(RoleConst.USER)
            .loginId("test")
            .password(passwordEncoder.encode("test"))
            .name("임현우")
            .build();

        customerRepository.save(testCustomer);
    }


}

