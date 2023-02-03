package Lim.boardApp.Controller;

import Lim.boardApp.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {
    
    //TODO : Filter & InterCeptor를 이용해서 whitelist 만들기.
    //TODO : validation 구현
    //TODO : validation까지 완료한 이후에 jwt를 이용해서 회원가입 구현하기.

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    //일반 홈 화면
    @GetMapping("/")
    public String home(@SessionAttribute(name = SessionConst.LOGIN_CUSTOMER, required = false) Customer loginCustomer, Model model) {
        if (loginCustomer == null) {
            return "home";
        }else {
            model.addAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer);
            return "redirect:/board";
        }
    }

    //회원가입 화면
    @GetMapping("/register")
    public String getAddCustomer(Model model) {
        CustomerRegisterForm customerRegisterForm = new CustomerRegisterForm();
        model.addAttribute("customer", customerRegisterForm);
        return "addCustomer";
    }

    //회원가입
    @PostMapping("/register")
    public String postAddCustomer(@Validated @ModelAttribute("customer") CustomerRegisterForm customerRegisterForm, BindingResult bindingResult) {

       if(customerService.dupLoginId(customerRegisterForm))
           bindingResult.reject("dupLoginId", "이미 등록된 아이디입니다.");

        if(bindingResult.hasErrors()){
            return "addCustomer";
        }
        Customer customer = new Customer().builder()
                        .loginId(customerRegisterForm.getLoginId())
                        .age(customerRegisterForm.getAge())
                        .name(customerRegisterForm.getName())
                        .password(customerRegisterForm.getPassword())
                        .build();
        customerRepository.save(customer);
        return "home";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        LoginForm loginForm = new LoginForm();
        model.addAttribute("loginForm", loginForm);
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm form, HttpServletRequest request) {
        Customer loginCustomer = customerService.login(form.getLoginId(), form.getPassword());
        if (loginCustomer == null) {
            return "login";
        } else {
            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer.getId());
            return "redirect:/board";
        }
    }

    @PostMapping("/logoutss")
    public String logout(HttpServletRequest request){
        customerService.logout(request);
        return "redirect:/";
    }
}
