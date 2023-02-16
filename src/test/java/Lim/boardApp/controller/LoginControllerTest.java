package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;


import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
class LoginControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;

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




}