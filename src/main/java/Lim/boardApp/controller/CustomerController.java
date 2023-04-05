package Lim.boardApp.controller;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.KakaoConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.EmailAuthForm;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.form.PasswordChangeForm;
import Lim.boardApp.service.CustomerService;
import Lim.boardApp.service.EmailService;
import Lim.boardApp.service.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CustomerController {

    private final CustomerService customerService;
    private final OauthService oauthService;
    private final EmailService emailService;

    //일반 홈 화면
    @GetMapping("/")
    public String home(@SessionAttribute(name = SessionConst.LOGIN_CUSTOMER, required = false) Long id, Model model) {
        if (id == null) {
            return "customer/home";
        }else {
            model.addAttribute(SessionConst.LOGIN_CUSTOMER, id);
            return "redirect:/board";
        }
    }

    //회원가입 화면
    @GetMapping("/register")
    public String getAddCustomer(@SessionAttribute(value = SessionConst.KAKAO_ID,required = false) Long kakaoId,
                                  Model model) {
        CustomerRegisterForm customerRegisterForm = new CustomerRegisterForm();
        model.addAttribute("customer", customerRegisterForm);

        if(kakaoId != null){
            model.addAttribute("kakaoId", kakaoId);
        }
        return "customer/addCustomer";
    }

    //일반회원가입
    @PostMapping("/register")
    public String postAddCustomer(@Validated @ModelAttribute("customer") CustomerRegisterForm customerRegisterForm, BindingResult bindingResult
                                    , @SessionAttribute(value = SessionConst.KAKAO_ID,required = false) Long kakaoId,
                                  HttpSession session) {

       if(customerService.dupLoginId(customerRegisterForm.getLoginId()))
           bindingResult.reject("dupLoginId", "이미 등록된 아이디입니다.");

        if(customerService.dupEmail(customerRegisterForm))
            bindingResult.reject("dupEmail", "이미 등록된 이메일주소입니다.");

       if(!customerRegisterForm.getPassword().equals(customerRegisterForm.getPasswordCheck()))
           bindingResult.reject("wrongPasswordInput", "입력하신 비밀번호와 비밀번호 확인이 다릅니다.");

       if(kakaoId != null && customerService.findKakao(kakaoId) != null){
           bindingResult.reject("dupKakaoId", "해당 카카오 계정으로 연동된 계정이 존재합니다.");
       }

       if(!emailService.checkEmailForm(customerRegisterForm.getEmail()))
           bindingResult.reject("invalidEmail", "유효한 이메일을 입력해주세요.");

        if(bindingResult.hasErrors()){
            return "customer/addCustomer";
        }

        //정상적인 회원가입
        if (kakaoId != null) {
            session.removeAttribute(SessionConst.KAKAO_ID);
            customerRegisterForm.setKakaoId(kakaoId);
        }

        customerService.addCustomer(customerRegisterForm);
        return "customer/home";
    }

    @GetMapping("/customer-login")
    public String loginForm(Model model,@RequestParam(value = "loginFail", required = false) String loginFail) {
        LoginForm loginForm = new LoginForm();
        if(loginFail != null){
            model.addAttribute("loginFail", "해당 카카오 계정으로 가입된 회원이 없습니다.");
        }
        model.addAttribute("loginForm", loginForm);
        return "customer/login";
    }

    @PostMapping("/customer-login")
    public String login(@Validated @ModelAttribute LoginForm form,BindingResult bindingResult,
                        @RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL,HttpSession session) {
        if (bindingResult.hasFieldErrors()) {
            return "customer/login";
        }
        Customer loginCustomer = customerService.login(form.getLoginId(), form.getPassword());
        if (loginCustomer == null) { //로그인 실패
            bindingResult.reject("loginFail","존재하지 않는 아이디이거나 잘못된 비밀번호입니다.");
            return "customer/login";
        }

        return "redirect:" + redirectURL;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        customerService.logout(request);
        return "redirect:/";
    }

    //카카오 로그인
    @GetMapping("/kakao/login")
    public String redirectionToKakaoLogin(){
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + KakaoConst.KEY + "&redirect_uri="
                + KakaoConst.REDIRECT_URL_LOGIN + "&response_type=code";
        return "redirect:"+ loginUrl;
    }

    @GetMapping("/oauth/kakao/login")
    public String kakaoLogin(@RequestParam("code")String code,RedirectAttributes attributes,
                          @RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL,HttpSession session){
        String accessToken = oauthService.getKakaoToken(code,"login");
        Long kakaoId = oauthService.getUserID(accessToken);

        Customer customer = customerService.findKakao(kakaoId);

        //해당 카카오 계정과 연동된 아이디가 존재하지않음
        if (customer == null) {
            attributes.addAttribute("loginFail", "fail");
            return "redirect:/login";
        }else{
            session.setAttribute(SessionConst.LOGIN_CUSTOMER, customer.getId());
            return "redirect:/";
        }
    }

    //카카오 계정 연동
    @GetMapping("/kakao/register")
    public String redirectionToKakaoRegister(){
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + KakaoConst.KEY + "&redirect_uri="
                + KakaoConst.REDIRECT_URL_REG + "&response_type=code";
        return "redirect:"+ loginUrl;
    }

    @GetMapping("/oauth/kakao/register")
    public String kakaoRegister(@RequestParam("code")String code, HttpSession session){
        String accessToken = oauthService.getKakaoToken(code,"register");
        Long kakaoId = oauthService.getUserID(accessToken);

        Customer customer = customerService.findKakao(kakaoId);

        //해당 카카오 계정과 연동된 아이디가 존재하지않음 -> 회원가입 가능
        if (customer == null) {
            session.setAttribute(SessionConst.KAKAO_ID, kakaoId);
        }
        return "redirect:/register";
    }


    //비밀번호 찾기 -> 이메일 인증
    @GetMapping("/find-password")
    public String emailAuthView (Model model){
        EmailAuthForm emailAuthForm = new EmailAuthForm();
        model.addAttribute("emailAuthForm", emailAuthForm);
        return "customer/emailAuth";
    }

    @PostMapping("/find-password")
    public String emailAuthCheck(@Validated @ModelAttribute EmailAuthForm emailAuthForm, BindingResult bindingResult){
        if(!emailService.checkEmailAuth(emailAuthForm.getEmail(), emailAuthForm.getEmailAuth())){
            //인증 실패
            bindingResult.reject("authFail", "이메일 인증에 실패하였습니다. 다시 정확하게 입력해주세요.");
        }

        if (bindingResult.hasErrors()) {
            return "customer/emailAuth";
            //인증 성공

        }
        Customer customer = customerService.findCustomerByEmail(emailAuthForm.getEmail());
            Long id = customer.getId();
            return "redirect:/new-password?id=" + id;

    }

    @GetMapping("/new-password")
    public String newPassword(Model model) {
        PasswordChangeForm form = new PasswordChangeForm();
        model.addAttribute("form", form);
        return "customer/newPassword";
    }

    @PostMapping("/new-password")
    public String newPasswordPost(@Validated @ModelAttribute("form") PasswordChangeForm form, BindingResult bindingResult, @RequestParam("id") Long id) throws NotFoundException {
        Customer customer = customerService.findCustomer(id);
        if (customer == null) {
            throw new NotFoundException();
        }
        if (!form.getPassword().equals(form.getPasswordCheck())) {
            bindingResult.reject("wrongPasswordInput","입력하신 비밀번호와 비밀번호 입력이 동일하지 않습니다. 다시 입력해주세요");
        }

        if (bindingResult.hasErrors()) {
            return "customer/newPassword";
        }

        customerService.changePassword(form.getPassword(), id);
        return "customer/changePasswordSuccess";
    }

}


