package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.RoleConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final CustomerRepository customerRepository;
    private final LoginService loginService;


    //일반 홈 화면
    @GetMapping("/")
    public String home(@SessionAttribute(name = SessionConst.LOGIN_CUSTOMER, required = false) Customer loginCustomer, Model model) {
        if (loginCustomer == null) {
            return "home";
        }else {
            model.addAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer.getId());
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

    //일반회원가입
    @PostMapping("/register")
    public String postAddCustomer(@Validated @ModelAttribute("customer") CustomerRegisterForm customerRegisterForm, BindingResult bindingResult) {

       if(loginService.dupLoginId(customerRegisterForm))
           bindingResult.reject("dupLoginId", "이미 등록된 아이디입니다.");

        if(bindingResult.hasErrors()){
            return "addCustomer";
        }
        Customer customer = new Customer().builder()
                        .loginId(customerRegisterForm.getLoginId())
                        .age(customerRegisterForm.getAge())
                        .name(customerRegisterForm.getName())
                        .password(customerRegisterForm.getPassword())
                        .role(RoleConst.USER)
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
    public String login(@RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL,
            @ModelAttribute LoginForm form,HttpSession session) {
        Customer loginCustomer = loginService.login(form.getLoginId(), form.getPassword());
        if (loginCustomer == null) { //로그인 실패
            return "login";
        } else {
            session.setAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer.getId());
            return "redirect:" + redirectURL;
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        loginService.logout(request);
        return "redirect:/";
    }
}
