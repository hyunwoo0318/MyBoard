package Lim.boardApp.Controller;

import Lim.boardApp.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {
    
    //TODO : Filter & InterCeptor를 이용해서 whitelist 만들기.
    //TODO : validation 구현
    //TODO : validation까지 완료한 이후에 jwt를 이용해서 회원가입 구현하기.

    private final CustomerRepository customerRepository;
    private final LoginService loginService;

    //일반 홈 화면
    @GetMapping("/")
    public String home(@SessionAttribute(name = SessionConst.LOGIN_CUSTOMER, required = false) Customer loginCustomer, Model model) {
        if (loginCustomer == null) {
            return "home";
        }else {
            model.addAttribute("loginCustomer", loginCustomer);
            return "redirect:/board";
        }
    }

    //회원가입 화면
    @GetMapping("/register")
    public String addCustomerForm(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);
        return "addcustomer";
    }

    //회원가입
    @PostMapping("/register")
    public String addCustomer(@ModelAttribute Customer customer) {
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
        Customer loginCustomer = loginService.login(form.getLoginId(), form.getPassword());
        if (loginCustomer == null) {
            return "login";
        } else {
            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer);
            return "redirect:/board";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        loginService.logout(request);
        return "/home";
    }
}
