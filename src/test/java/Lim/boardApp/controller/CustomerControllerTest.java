package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.LoginForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.CustomerService;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;


import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;


    @Test
    @DisplayName("로그인 하지 않은 사용자가 홈 화면에 접근할때 - /")
    public void homeWithoutLogin() throws Exception{
        MockHttpSession sessionWithoutLogin = new MockHttpSession();
        mockMvc.perform(get("/")
                .session(sessionWithoutLogin))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andDo(print());
    }

    @BeforeEach
    public void delete(){
        customerRepository.deleteAll();
    }


    @Test
    @DisplayName("로그인 한 사용자가 홈 화면에 접근할때 - /")
    public void homeWithLogin() throws Exception {
        Customer loginCustomer = new Customer();
        customerRepository.save(loginCustomer);
        mockMvc.perform(get("/")
                        .sessionAttr(SessionConst.LOGIN_CUSTOMER, loginCustomer))
                        .andExpect(redirectedUrl("/board"))
                        .andDo(print());
    }
    @Test
    @DisplayName("로그아웃 테스트 - /logout")
    public void logoutTest() throws Exception{
        Customer loginCustomer = new Customer();
        customerRepository.save(loginCustomer);

        MockHttpSession loginSession = new MockHttpSession();
        loginSession.setAttribute(SessionConst.LOGIN_CUSTOMER, loginCustomer.getId());

        MvcResult mvcResult = mockMvc.perform(post("/logout").session(loginSession)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
        assertThat(mvcResult.getRequest().getSession().getAttribute(SessionConst.LOGIN_CUSTOMER)).isNull();
        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    @DisplayName("회원가입 화면 테스트 - /register")
    public void registerViewTest() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm();

        MvcResult mvcResult = mockMvc.perform(get("/register").sessionAttr(SessionConst.EMAIL,"ex@ex.com")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getModelMap().get("customer").getClass()).isEqualTo(CustomerRegisterForm.class);
        assertThat(mvcResult.getModelAndView().getModelMap().get("customer")).isEqualTo(form);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("addCustomer");
    }

    @Test
    @DisplayName("이메일 인증을 거치지 않고 회원가입 화면을 시도하는 경우 - /register")
    public void registerViewWithoutEmailAuth() throws Exception{
        MvcResult mvcResult = mockMvc.perform(get("/register")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }


    @Test
    @DisplayName("회원가입 테스트(정상적인 회원가입) - /register")
    public void registerSuccessTest() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hyunwoo", 23);
        MvcResult mvcResult = mockMvc.perform(post("/register").flashAttr("customer", form).sessionAttr(SessionConst.EMAIL,"ex@ex.com")
                .sessionAttr(SessionConst.KAKAO_ID, 23)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("home");

        Optional<Customer> customerOptional = customerRepository.findByLoginId("id123123");
        assertThat(customerOptional).isNotEmpty();

        Customer c = customerOptional.get();
        assertThat(c.getLoginId()).isEqualTo(form.getLoginId());
        assertThat(c.getName()).isEqualTo(form.getName());
        assertThat(c.getAge()).isEqualTo(form.getAge());
        assertThat(c.getEmail()).isEqualTo("ex@ex.com");
        assertThat(c.getKakaoId()).isEqualTo(23L);
    }

    @Test
    @DisplayName("회원가입 테스트(중복된 아이디로 회원가입 시도) - /register")
    public void registerDupLoginIdTest() throws Exception{
        Customer customer = new Customer("id123123", "pw123123", "hyunwoo", 23, "USER","ex@ex.com");
        customerRepository.save(customer);

        CustomerRegisterForm form = new CustomerRegisterForm("id123123", "pw123456","pw123456", "john", 25);

        MvcResult mvcResult = mockMvc.perform(post("/register").flashAttr("customer", form).sessionAttr(SessionConst.EMAIL,"ex2@ex2.com")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("addCustomer");

        Customer c = customerRepository.findByLoginId("id123123").get();
        assertThat(c.getAge()).isEqualTo(23);
        assertThat(c.getName()).isEqualTo("hyunwoo");
        assertThat(c.getEmail()).isEqualTo("ex@ex.com");
    }

    @Test
    @DisplayName("회원가입 테스트(비밀번호와 비밀번호 입력이 다른경우) - /register")
    public void registerInvalidPasswordCheck() throws Exception{

        CustomerRegisterForm formDiffer = new CustomerRegisterForm("id123123", "pw123123", "pw456456", "hyunwoo", 23);

        MvcResult result = mockMvc.perform(post("/register").flashAttr("customer", formDiffer)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getModelAndView().getViewName()).isEqualTo("addCustomer");
        assertThat(customerRepository.findByLoginId("id123123")).isEmpty();
    }


    @Test
    @DisplayName("회원가입 테스트(유효하지 않은 입력으로 회원가입 시도) - /register")
    public void registerInvalidateFormTest() throws Exception {
        customerRepository.deleteAll();
        CustomerRegisterForm formNoName = new CustomerRegisterForm("id123123", "pw123456", "pw123456", "", 25);
        CustomerRegisterForm formNoPw = new CustomerRegisterForm("id123123", "", "pw123456", "john", 25);

        MvcResult result1 = mockMvc.perform(post("/register").flashAttr("customer", formNoName)).andReturn();
        MvcResult result2 = mockMvc.perform(post("/register").flashAttr("customer", formNoPw)).andReturn();

        assertThat(result1.getResponse().getStatus()).isEqualTo(200);
        assertThat(result1.getModelAndView().getViewName()).isEqualTo("addCustomer");
        assertThat(result2.getResponse().getStatus()).isEqualTo(200);
        assertThat(result2.getModelAndView().getViewName()).isEqualTo("addCustomer");

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("로그인 화면 테스트 - /login")
    public void loginViewTest() throws Exception{
        MvcResult mvcResult = mockMvc.perform(get("/login")).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getModel().get("loginForm")).isEqualTo(new LoginForm());
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("login");
    }

    @Test
    @DisplayName("로그인(성공) 테스트 - /login")
    public void loginSuccess() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hyunwoo", 23);
        customerService.addCustomer(form,20);
        Long id = customerRepository.findByLoginId("id123123").get().getId();

        String redirectURL = "/board";
        LoginForm loginForm = new LoginForm("id123123", "pw123123");
        MvcResult mvcResult = mockMvc.perform(post("/login")
                .flashAttr("loginForm", loginForm)
                .param("redirectURL", redirectURL)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(redirectURL);
        assertThat(mvcResult.getRequest().getSession().getAttribute(SessionConst.LOGIN_CUSTOMER)).isEqualTo(id);
    }

    @Test
    @DisplayName("로그인(잘못된 아이디/비밀번호) 테스트")
    public void loginFail() throws Exception{
        CustomerRegisterForm form = new CustomerRegisterForm("id123123", "pw123123","pw123123", "hyunwoo", 23);
        customerService.addCustomer(form,20);

        LoginForm wrongPw = new LoginForm("id123123", "pw456456");
        LoginForm wrongId = new LoginForm("id456456", "pw123123");
        LoginForm wrongIdAndPw = new LoginForm("id456456", "pw456456");

        MvcResult result1 = mockMvc.perform(post("/login").flashAttr("loginForm", wrongPw)).andReturn();
        MvcResult result2 = mockMvc.perform(post("/login").flashAttr("loginForm", wrongId)).andReturn();
        MvcResult result3 = mockMvc.perform(post("/login").flashAttr("loginForm", wrongIdAndPw)).andReturn();

        assertThat(result1.getResponse().getStatus()).isEqualTo(200);
        assertThat(result1.getModelAndView().getViewName()).isEqualTo("login");
        assertThat(result1.getRequest().getSession().getAttribute(SessionConst.LOGIN_CUSTOMER)).isNull();

        assertThat(result2.getResponse().getStatus()).isEqualTo(200);
        assertThat(result2.getModelAndView().getViewName()).isEqualTo("login");
        assertThat(result2.getRequest().getSession().getAttribute(SessionConst.LOGIN_CUSTOMER)).isNull();

        assertThat(result3.getResponse().getStatus()).isEqualTo(200);
        assertThat(result3.getModelAndView().getViewName()).isEqualTo("login");
        assertThat(result3.getRequest().getSession().getAttribute(SessionConst.LOGIN_CUSTOMER)).isNull();
    }



}