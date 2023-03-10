package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.SessionConst;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.CustomerRegisterForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.service.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class TextControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired private TextService textService;
    @Autowired private CommentService commentService;
    @Autowired private CustomerService customerService;
    @Autowired private TextHashtagService textHashtagService;
    @Autowired private HashtagService hashtagService;

    @Autowired private CustomerRepository customerRepository;

    private Long id;
    /**
     * 100개의 임의의 text저장
     */
    @BeforeEach
    public void init(){
        customerService.addCustomer(new CustomerRegisterForm("id123123","pw123123","pw123123","hyunwoo",23),20);
        Customer customer = customerService.findCustomer("id123123");
        id = customer.getId();
        List<Hashtag> hashtagList = new ArrayList<>();

        for(int i=1;i<=100;i++){
            Hashtag hashtag = hashtagService.addHashtag("h" + i);
            hashtagList.add(hashtag);
            textService.createText(id, new TextCreateForm("title" + i, "content" + (100 - i)), hashtagList);
        }
    }

    @AfterAll
    public void deleteAll(){
        customerRepository.deleteAllInBatch();
    }

   @Test
    @DisplayName("글 리스트 화면 테스트 - /board")
    public void textListViewTest() throws Exception{
        MvcResult mvcResult = mockMvc.perform(get("/board").param("page", "3")
                .sessionAttr(SessionConst.LOGIN_CUSTOMER, id)).andReturn();
        List<Text> textList = new ArrayList<>();
        for(int i=31;i<=40;i++){
            Text text = textService.findText("title" + i).get(0);
            textList.add(text);
        }
        PageForm expectForm = new PageForm(0,4,10,3,9,textList,false,false);

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/textList");
        assertThat(mvcResult.getModelAndView().getModel().get("pageForm")).isEqualTo(expectForm);
        assertThat(mvcResult.getModelAndView().getModel().get("searchKey")).isEqualTo("");
        assertThat(mvcResult.getModelAndView().getModel().get("type")).isEqualTo("");
    }

   @Test
    @DisplayName("검색 테스트(제목) - /search")
    public void searchByTitle() throws Exception{
        /**
         * searchKey = "title1", type=title
         * Expect result titleList = "title1, title10 ,title11,title12,title13,title14,title15,title16,title17,title18,title19, title100"
         */
        String searchKey = "title1";
        String type = "title";
        List<Text> textList = new ArrayList<>();
        textList.add(textService.findText("title1").get(0));
        for(int i=10;i<=18;i++){
            textList.add(textService.findText("title" + i).get(0));
        }

        PageForm expectForm = new PageForm(0,4,10,0,1,textList,false,true);

        MvcResult mvcResult = mockMvc.perform(get("/board/search").sessionAttr(SessionConst.LOGIN_CUSTOMER, id)
                .param("page", "0")
                .param("searchKey", searchKey)
                .param("type", type)).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/textList");
        assertThat(mvcResult.getModelAndView().getModel().get("pageForm")).isEqualTo(expectForm);
    }
}