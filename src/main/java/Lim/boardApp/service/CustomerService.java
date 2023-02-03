package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    //서비스와 form의 독립성을 위해서 loginForm을 인자로 넘기지 않음
    //로그인 실패시 null을, 성공시 customer을 리턴함.
    public Customer login(String loginId, String password){
        return customerRepository.findByLoginId(loginId).stream()
                .filter(customer -> customer.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    //로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public boolean dupLoginId(CustomerRegisterForm customerRegisterForm){
        List<Customer> dupLoginId = customerRepository.findByLoginId(customerRegisterForm.getLoginId());
        if(dupLoginId.isEmpty()) return false;
        else return true;
    }

}
