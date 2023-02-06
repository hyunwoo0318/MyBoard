package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final CustomerRepository customerRepository;
    private final AuthenticationManagerBuilder authenticationManager;


    //서비스와 form의 독립성을 위해서 loginForm을 인자로 넘기지 않음
    //로그인 실패시 null을, 성공시 customer을 리턴함.
    public Customer login(String loginId, String password){
        Optional<Customer> customerOptional = customerRepository.findByLoginId(loginId);
        if(customerOptional.isEmpty()) {
            return null;
        }
        Customer customer = customerOptional.get();
        if(customer.getPassword() == password){
            return customer;
        }else{
            return null;
        }

    }

    //로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public boolean dupLoginId(CustomerRegisterForm customerRegisterForm){
        Optional<Customer> dup = customerRepository.findByLoginId(customerRegisterForm.getLoginId());
        if(dup.isEmpty()) return false;
        else return true;
    }

}
