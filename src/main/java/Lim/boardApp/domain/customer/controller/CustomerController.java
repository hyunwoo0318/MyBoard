package Lim.boardApp.domain.customer.controller;

import Lim.boardApp.common.email.EmailService;
import Lim.boardApp.common.exception.BindingResultHelper;
import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.domain.customer.dto.CustomerProfileDto;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.customer.form.CustomerRegisterForm;
import Lim.boardApp.domain.customer.form.EmailAuthForm;
import Lim.boardApp.domain.customer.form.LoginForm;
import Lim.boardApp.domain.customer.form.PasswordChangeForm;
import Lim.boardApp.domain.customer.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CustomerController {

    private final CustomerService customerService;
    private final EmailService emailService;

    // 회원가입 화면
    @GetMapping("/register")
    public String getAddCustomer(Model model) {
        model.addAttribute("customer", new CustomerRegisterForm());

        return "customer/addCustomer";
    }

    // 일반회원가입
    @PostMapping("/register")
    public String postAddCustomer(
            @Valid @ModelAttribute("customer") CustomerRegisterForm customerRegisterForm,
            BindingResult bindingResult) {

        //DTO Validation Check
        if (bindingResult.hasErrors()) {
            return "customer/addCustomer";
        }

        try{
            customerService.addCustomer(customerRegisterForm);
            return "redirect:/";
        }catch (CustomException customException){
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "customer/addCustomer";
        }

    }

    @GetMapping("/customer-login")
    public String loginForm(Model model) {
        LoginForm loginForm = new LoginForm();

        model.addAttribute("loginForm", loginForm);
        return "customer/login";
    }

    @PostMapping("/customer-login")
    public String login(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult) {

        //DTO Validation check
        if(bindingResult.hasErrors()){
            return "customer/login";
        }

        try{
            customerService.login(form);
            return "redirect:/";
        }catch (CustomException customException){
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "customer/login";
        } catch (BadCredentialsException badCredentialsException){
            bindingResult.reject("loginFail", "로그인에 실패했습니다.");
            return "customer/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        customerService.logout(request);
        return "redirect:/";
    }

    /** 가입시 등록한 이메일을 통해 비밀번호 찾기 구현 */

    // 비밀번호 찾기 -> 이메일 인증
    @GetMapping("/find-password")
    public String emailAuthView(Model model) {
        EmailAuthForm emailAuthForm = new EmailAuthForm();
        model.addAttribute("emailAuthForm", emailAuthForm);
        return "customer/emailAuth";
    }

    @PostMapping("/find-password")
    public String emailAuthCheck(
            @Valid @ModelAttribute EmailAuthForm emailAuthForm, BindingResult bindingResult) {

        //DTO Validation Check
        if (bindingResult.hasErrors()) {
            return "customer/emailAuth";
        }

        try{
            Long customerId = emailService.checkEmailAuth(emailAuthForm.getEmail(),
                emailAuthForm.getEmailAuth());

            return "redirect:/new-password?id=" + customerId;
        }catch (CustomException customException){
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "customer/emailAuth";
        }
    }

    /** 이메일 인증이 성공하면 새로운 비밀번호로 변경 (기존의 비밀번호는 알수없음) */
    @GetMapping("/new-password")
    public String newPassword(Model model) {
        PasswordChangeForm form = new PasswordChangeForm();
        model.addAttribute("form", form);
        return "customer/newPassword";
    }

    @PostMapping("/new-password")
    public String newPasswordPost(
            @Validated @ModelAttribute("form") PasswordChangeForm form, BindingResult bindingResult,
            @RequestParam("id") Long id) {

        //DTO Validation check
        if (bindingResult.hasErrors()) {
            return "customer/newPassword";
        }

        try{
            customerService.changePassword(form, id);
            return "customer/changePasswordSuccess";
        }catch (CustomException customException){
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "customer/newPassword";
        }
    }

    /** 내 정보 확인 */
    @GetMapping("/profiles/{loginId}")
    public String getProfile(
            @PathVariable("loginId") String loginId,
            @AuthenticationPrincipal(errorOnInvalidType = true) Customer customer,
            BindingResult bindingResult,
            Model model) {

        if (!customer.getLoginId().equals(loginId)) {
            return "redirect:/";
        }

        try{
            CustomerProfileDto customerProfileDto = customerService.getCustomerProfile(loginId);

            model.addAttribute("customerProfile", customerProfileDto);
            return "customer/profile";
        } catch (CustomException customException){
            BindingResultHelper.setBindingResult(customException, bindingResult);
            return "redirect:/";
        }



    }
}
