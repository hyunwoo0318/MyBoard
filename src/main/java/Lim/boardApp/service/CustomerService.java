package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;



}
