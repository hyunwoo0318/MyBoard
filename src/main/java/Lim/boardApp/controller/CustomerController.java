package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.KakaoConst;
import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.LoginForm;
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
    public String getAddCustomer(@SessionAttribute(value = "email") String email, @SessionAttribute(value = "kakaoId",required = false) Long kakaoId,
                                  Model model) {
        CustomerRegisterForm customerRegisterForm = new CustomerRegisterForm();
        model.addAttribute("customer", customerRegisterForm);
        model.addAttribute("email", email);
        if(kakaoId != null){
            model.addAttribute("kakaoId", kakaoId);
        }
        return "addCustomer";
    }

    //일반회원가입
    @PostMapping("/register")
    public String postAddCustomer(@Validated @ModelAttribute("customer") CustomerRegisterForm customerRegisterForm, BindingResult bindingResult
                                    ,@SessionAttribute(value = "email") String email, @SessionAttribute(value = "kakaoId",required = false) Long kakaoId,
                                  HttpSession session) {

       if(customerService.dupLoginId(customerRegisterForm))
           bindingResult.reject("dupLoginId", "이미 등록된 아이디입니다.");

       if(!customerRegisterForm.getPassword().equals(customerRegisterForm.getPasswordCheck()))
           bindingResult.reject("wrongPasswordInput", "입력하신 비밀번호와 비밀번호 확인이 다릅니다.");

       if(kakaoId != null && customerService.findKakao(kakaoId) != null){
           bindingResult.reject("dupKakaoId", "해당 카카오 계정으로 연동된 계정이 존재합니다.");
       }

        if(bindingResult.hasErrors()){
            return "addCustomer";
        }

        //정상적인 회원가입
        if (kakaoId != null) {
            session.removeAttribute(SessionConst.KAKAO_ID);
            customerRegisterForm.setKakaoId(kakaoId);
        }

        customerRegisterForm.setEmail(email);
        session.removeAttribute(SessionConst.EMAIL);

        customerService.addCustomer(customerRegisterForm, 20);
        return "home";
    }

    @GetMapping("/login")
    public String loginForm(Model model,@RequestParam(value = "loginFail", required = false) String loginFail) {
        LoginForm loginForm = new LoginForm();
        if(loginFail != null){
            model.addAttribute("loginFail", "해당 카카오 계정으로 가입된 회원이 없습니다.");
        }
        model.addAttribute("loginForm", loginForm);
        return "login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginForm form,BindingResult bindingResult,
                        @RequestParam(value = "redirectURL", defaultValue = "/") String redirectURL,HttpSession session) {
        if (bindingResult.hasFieldErrors()) {
            return "login";
        }
        Customer loginCustomer = customerService.login(form.getLoginId(), form.getPassword());
        if (loginCustomer == null) { //로그인 실패
            bindingResult.reject("loginFail","존재하지 않는 아이디이거나 잘못된 비밀번호입니다.");
            return "login";
        } else {
            session.setAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer.getId());
            return "redirect:" + redirectURL;
        }
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


    //이메일 인증
    @GetMapping("/auth/email")
    public String emailAuthView (@RequestParam(value = "email", required = false) String email ,Model model){
        String emailAuth = "";
        Boolean isEmail = emailService.checkEmailForm(email);
        if(email==null){
            //첫시도
            model.addAttribute("email","");
        }else if(!isEmail){
            //잘못된 이메일 입력을 하는경우
            model.addAttribute("emailFormError", "정확한 형식의 이메일을 입력해주세요.");
            model.addAttribute("email", email);
        }else if(!emailService.findPrevAuth(email)){
            //인증메일을 보내지 않아 보내야하는경우
            try{
                emailService.sendEmailAuth(email);
            } catch(Exception e){
                e.printStackTrace();
            }
            model.addAttribute("email", email);
        }else{
            //이미 인증메일을 보내고 유효시간내인경우
            model.addAttribute("email", email);
        }
        model.addAttribute("emailAuth", emailAuth);
        return "emailAuth";
    }

    @PostMapping("/auth/email")
    public String emailAuthCheck(@RequestParam("email") String email, @ModelAttribute("emailAuth") String emailAuth, BindingResult bindingResult,
                                 HttpSession session) {
        if(!emailService.checkEmailAuth(email,emailAuth)){
            //인증 실패
            bindingResult.reject("authFail", "이메일 인증에 실패하였습니다. 다시 정확하게 입력해주세요.");
            return "emailAuth";
        }else{
            //인증 성공
            session.setAttribute(SessionConst.EMAIL, email);
            return "redirect:/register";
        }
    }

}
