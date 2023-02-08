package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final CustomerRepository customerRepository;
    public Customer login(String loginId, String password){
        Optional<Customer> customerOptional = customerRepository.findByLoginId(loginId);
        if(customerOptional.isEmpty()) {
            return null;
        }
        Customer customer = customerOptional.get();
        if(customer.getPassword().equals(password)){
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
