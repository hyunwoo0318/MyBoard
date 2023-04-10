package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.CustomerService;
import Lim.boardApp.service.EmailService;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;


import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CustomerControllerTest {

    @Autowired WebApplicationContext context;

    @Autowired  MockMvc mockMvc;

    @Autowired CustomerRepository customerRepository;

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;

    private Customer customer;

    @BeforeEach
    public void deleteAll() {
        customerRepository.deleteAllInBatch();
        customer = Customer.builder()
                .loginId("user1")
                .password(passwordEncoder.encode("pw1"))
                .name("hyunwoo")
                .age(20)
                .email("ex@naver.com")
                .role("USER")
                .build();
        customerRepository.saveAndFlush(customer);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("로그인 한 사용자가 홈 화면에 접근할때 - /")
    @WithUserDetails(value = "user1",setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void homeWithLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(redirectedUrl("/board"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자가 홈 화면에 접근할때 - /")
    public void homeWithoutLogin() throws Exception{
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/home"))
                .andDo(print());
    }



    @Test
    @DisplayName("로그아웃 테스트 - /logout")
    @WithUserDetails("user1")
    public void logoutTest() throws Exception{
        MvcResult mvcResult = mockMvc.perform(post("/logout")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/customer-login?logout");
    }

    @Test
    @DisplayName("회원가입 화면 테스트 - /register")
    public void registerViewTest() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm();

        MvcResult mvcResult = mockMvc.perform(get("/register")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getModelMap().get("customer").getClass()).isEqualTo(CustomerRegisterForm.class);
        assertThat(mvcResult.getModelAndView().getModelMap().get("customer")).isEqualTo(form);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");
    }


    @Test
    @DisplayName("회원가입 테스트(정상적인 회원가입) - /register")
    public void registerSuccessTest() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hyunwoo",  23, null,"hyunwoo0318@naver.com");
        MvcResult mvcResult = mockMvc.perform(post("/register").flashAttr("customer", form)
                .sessionAttr(SessionConst.KAKAO_ID, 23)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("customer/home");

        Optional<Customer> customerOptional = customerRepository.findByLoginId("id123123");
        assertThat(customerOptional).isNotEmpty();

        Customer c = customerOptional.get();
        assertThat(c.getLoginId()).isEqualTo(form.getLoginId());
        assertThat(c.getName()).isEqualTo(form.getName());
        assertThat(c.getAge()).isEqualTo(form.getAge());
        assertThat(c.getKakaoId()).isEqualTo(23L);
    }

    @Test
    @DisplayName("회원가입 테스트(중복된 아이디로 회원가입 시도) - /register")
    public void registerDupLoginIdTest() throws Exception{
       CustomerRegisterForm form = new CustomerRegisterForm("user1", "pw123456","pw123456", "john", 25);

        MvcResult mvcResult = mockMvc.perform(post("/register").flashAttr("customer", form)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");

        Customer c = customerRepository.findByLoginId("user1").get();
        assertThat(c.getAge()).isEqualTo(20);
        assertThat(c.getName()).isEqualTo("hyunwoo");
    }

    @Test
    @DisplayName("회원가입 테스트(비밀번호와 비밀번호 입력이 다른경우) - /register")
    public void registerInvalidPasswordCheck() throws Exception{

        CustomerRegisterForm formDiffer = new CustomerRegisterForm("id123123", "pw123123", "pw456456", "hyunwoo", 23);

        MvcResult result = mockMvc.perform(post("/register").flashAttr("customer", formDiffer)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");
        assertThat(customerRepository.findByLoginId("id123123")).isEmpty();
    }


    @Test
    @DisplayName("회원가입 테스트(유효하지 않은 입력으로 회원가입 시도) - /register")
    public void registerInvalidateFormTest() throws Exception {
        CustomerRegisterForm formNoName = new CustomerRegisterForm("id123123", "pw123456", "pw123456", "", 25,null,"hyunwoo0318@naver.com");
        CustomerRegisterForm formNoPw = new CustomerRegisterForm("id123123", "", "pw123456", "john", 25,null,"hyunwoo0318@naver.com");
        CustomerRegisterForm formNoEmail = new CustomerRegisterForm("id123123", "pw123456", "pw123456", "john", 25,null,"");

        MvcResult result1 = mockMvc.perform(post("/register").flashAttr("customer", formNoName)
                ).andReturn();
        MvcResult result2 = mockMvc.perform(post("/register").flashAttr("customer", formNoPw)
                ).andReturn();
        MvcResult result3 = mockMvc.perform(post("/register").flashAttr("customer", formNoEmail)
        ).andReturn();

        assertThat(result1.getResponse().getStatus()).isEqualTo(200);
        assertThat(result1.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");
        assertThat(result2.getResponse().getStatus()).isEqualTo(200);
        assertThat(result2.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");
        assertThat(result3.getResponse().getStatus()).isEqualTo(200);
        assertThat(result3.getModelAndView().getViewName()).isEqualTo("customer/addCustomer");
    }

    @Test
    @DisplayName("로그인 화면 테스트 - /customer-login")
    public void loginViewTest() throws Exception{
        MvcResult mvcResult = mockMvc.perform(get("/customer-login")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getModel().get("loginForm")).isEqualTo(new LoginForm());
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("customer/login");
    }

    @Test
    @DisplayName("로그인(성공) 테스트 - /customer-login")
    public void loginSuccess() throws Exception{
        String redirectURL = "board";
        LoginForm loginForm = new LoginForm("user1", "pw1");
        MvcResult mvcResult = mockMvc.perform(post("/customer-login")
                .flashAttr("loginForm", loginForm)
                .param("redirectURL", redirectURL)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(redirectURL);
    }

    @Test
    @DisplayName("로그인(잘못된 아이디/비밀번호) 테스트 - /customer-login")
    @Transactional
    public void loginFail() throws Exception{

        LoginForm wrongPw = new LoginForm("user1", "pw456456");
        LoginForm wrongId = new LoginForm("user123123", "pw1");
        LoginForm wrongIdAndPw = new LoginForm("id456456", "pw456456");

        MvcResult result1 = mockMvc.perform(post("/customer-login").flashAttr("loginForm", wrongPw)).andReturn();
        MvcResult result2 = mockMvc.perform(post("/customer-login").flashAttr("loginForm", wrongId)).andReturn();
        MvcResult result3 = mockMvc.perform(post("/customer-login").flashAttr("loginForm", wrongIdAndPw)).andReturn();

        assertThat(result1.getResponse().getStatus()).isEqualTo(200);
        assertThat(result1.getModelAndView().getViewName()).isEqualTo("customer/login");

        assertThat(result2.getResponse().getStatus()).isEqualTo(200);
        assertThat(result2.getModelAndView().getViewName()).isEqualTo("customer/login");

        assertThat(result3.getResponse().getStatus()).isEqualTo(200);
        assertThat(result3.getModelAndView().getViewName()).isEqualTo("customer/login");
    }


    @Test
    @DisplayName("비밀번호 찾기 화면 테스트 - /find-password")
    public void emailAuthViewTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/find-password")).andReturn();

        assertThat(result.getModelAndView().getViewName()).isEqualTo("customer/emailAuth");
    }




}