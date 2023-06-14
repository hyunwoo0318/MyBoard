package Lim.boardApp.controller;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.ObjectValue.RoleConst;
import Lim.boardApp.domain.*;
import Lim.boardApp.form.CommentForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.HashtagRepository;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.comment.CommentRepository;
import Lim.boardApp.repository.text.TextRepository;
import Lim.boardApp.repository.texthashtag.TextHashtagRepository;
import Lim.boardApp.service.HashtagService;
import Lim.boardApp.service.TextService;
import de.cronn.testutils.h2.H2Util;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.Filter;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ContextConfiguration
@Sql(scripts = {"classpath:db/initUser.sql"})
class TextControllerTest {

    @Autowired
    private TextService textService;
    @Autowired private HashtagService hashtagService;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private BoardRepository boardRepository;

    @Autowired private TextRepository textRepository;
    @Autowired private TextHashtagRepository textHashtagRepository;

    @Autowired private HashtagRepository hashtagRepository;

    @Autowired private CommentRepository commentRepository;

    @Autowired private BookmarkRepository bookmarkRepository;

    @Autowired private MockMvc mockMvc;

    private Board soccer, basketBall;
    private Customer user1, user2;

    private Text text1, text2;

    private Hashtag h1,h2,h3;

    private void baseInit(){
        //board 2종류 저장
        soccer = new Board("soccer");
        basketBall = new Board("basketBall");
        boardRepository.saveAndFlush(soccer);
        boardRepository.saveAndFlush(basketBall);

        List<Customer> customerList = customerRepository.findAll();
        user1 = customerList.get(0);
        user2 = customerList.get(1);

        //text1,2 저장 text1 -> soccer, text2 -> basketball
        text1 = Text.builder()
                .board(soccer)
                .title("title1")
                .content("content1")
                .customer(user1)
                .build();
        text2 = Text.builder()
                .board(basketBall)
                .title("title2")
                .content("content2")
                .customer(user1)
                .build();

        textRepository.saveAndFlush(text1);
        textRepository.saveAndFlush(text2);

        //hashtag h1,h2,h3 저장
        h1 = new Hashtag("h1");
        h2 = new Hashtag("h2");
        h3 = new Hashtag("h3");

        hashtagRepository.saveAndFlush(h1);
        hashtagRepository.saveAndFlush(h2);
        hashtagRepository.saveAndFlush(h3);
    }

    @Nested
    @DisplayName("글 리스트 화면 테스트 - /")
    public class showTextList {

        private String URL = "/";

        @BeforeEach
        public void setup(){
            baseInit();

            List<Text> textList = new ArrayList<>();
            //글 30개 추가
            for(int i=3;i<33;i++){
                Text text = Text.builder()
                        .board(soccer)
                        .title("title" + i)
                        .content("content" + i)
                        .customer(user1)
                        .build();
                textList.add(text);
            }

            textRepository.saveAllAndFlush(textList);
        }

        @Test
        @DisplayName("뷰 테스트")
        public void showTextViewTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/textList");
        }

        @Test
        @DisplayName("모델(searchKey, type) 테스트")
        public void showTextModelTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            Map<String, Object> model = mvcResult.getModelAndView().getModel();

            assertThat(model.get("searchKey")).isEqualTo("");
            assertThat(model.get("type")).isEqualTo("");
        }

        @Test
        @DisplayName("페이징 테스트")
        public void showTextPagingTest() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get(URL).queryParam("page", "2")).andReturn();

            PageForm expectPageForm = textService.pagingByAll(2, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, "전체");
            assertThat(expectPageForm.equals(mvcResult.getModelAndView().getModel().get("pageForm"))).isTrue();

        }



    }

    @Test
    @DisplayName("글 검색 테스트 - /search")
    public void searchTextListTest() throws Exception{

    }


    @Nested
    @DisplayName("특정 글 조회 - /show/{id}")
    public class showTextTest {

        private Long textId;
        private String URL = "/show/";

        private Comment comment1, comment2, comment3;

        @BeforeEach
        public void setText1() {
            baseInit();
            //text1에 h1,h2해시태그 추가
            TextHashtag th1 = new TextHashtag(text1, h1);
            TextHashtag th2 = new TextHashtag(text1, h2);

            textHashtagRepository.save(th1);
            textHashtagRepository.save(th2);

            /**
             * 3개의 댓글 추가
             * comment1
             *    ㄴ comment2
             * comment3
             */
            comment1 = new Comment(text1, user1, "comment1");
            comment2 = new Comment(text1, user1, "comment2", comment1);
            comment3 = new Comment(text1, user1, "comment3");

            commentRepository.saveAllAndFlush(Arrays.asList(comment1, comment2, comment3));

            textId = text1.getId();
            URL += textId;
        }

        @DisplayName("뷰 테스트")
        @Test
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void showTextViewTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/showText");
        }


        @DisplayName("주인이 자신의 글을 조회하는 경우")
        @Test
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void showTextByOwnerTest() throws Exception{
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(mvcResult.getModelAndView().getModel().get("owner")).isEqualTo(true);
        }

        @DisplayName("다른 사람의 글을 조회하는 경우")
        @Test
        @WithUserDetails(value = "id456", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void showTextByOtherTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(mvcResult.getModelAndView().getModel().get("owner")).isEqualTo(false);
        }

        @DisplayName("정상적으로 모델에 모든 값이 들어있는지 테스트")
        @Test
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void showTextModelTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            Map<String, Object> model = mvcResult.getModelAndView().getModel();
            assertThat(model.get("customerId")).isEqualTo(user1.getId());
            assertThat(model.get("textId")).isEqualTo(textId);
            assertThat(model.get("commentForm").getClass()).isEqualTo(CommentForm.class);

            ArrayList<Hashtag> hashtagList = (ArrayList<Hashtag>) model.get("hashtagList");
            List<String> hashtagNameList = hashtagList.stream().map(h -> h.getName()).collect(Collectors.toList());
            assertThat(hashtagNameList.size()).isEqualTo(2);
            assertThat(hashtagNameList).contains("h1", "h2");

            assertThat(model.get("isBookmarked")).isEqualTo(false);
            assertThat(model.get("commentCnt")).isEqualTo(3);

            ArrayList<Comment> commentList = (ArrayList<Comment>) model.get("commentList");
            List<String> commentContentList = commentList.stream().map(c -> c.getContent()).collect(Collectors.toList());
            assertThat(commentContentList.size()).isEqualTo(2);
            assertThat(commentContentList).contains("comment1", "comment3");
        }



    }

    @Nested
    @DisplayName("글 작성 - /new")
    public class makeText {

        //TODO : form이 안넘어가는 현상 해결해야함.
        private String URL = "/new";

        @BeforeEach
        public void init(){
            baseInit();
        }

        @Test
        @DisplayName("뷰 테스트")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void makeTextViewTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/makeText");
        }

        @Test
        @DisplayName("글 생성 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void makeTextSuccessTest() throws Exception {
            TextCreateForm form = new TextCreateForm("title4", "content4", "h1,h2,h3", "soccer", null);
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", form)).andReturn();

            //생성이 완료하여 게시판 메인 화면으로 리다이렉트 되는지 검증
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/");

            //전달된 form대로 글이 잘 생성되었는지 확인
            Text text4 = textRepository.findByTitle("title4").get(0);
            assertThat(text4.getTitle()).isEqualTo("title4");
            assertThat(text4.getContent()).isEqualTo("content4");
            assertThat(text4.getBoard().getName()).isEqualTo("soccer");
            assertThat(text4.getCustomer().getId()).isEqualTo(user1.getId());

            List<Hashtag> hashtagList = textHashtagRepository.findHashtagsByText(text4);
            List<String> hashtagNameList = hashtagList.stream().map(h -> h.getName()).collect(Collectors.toList());
            assertThat(hashtagNameList).contains("h1", "h2", "h3");
            assertThat(hashtagNameList.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("글 생성 실패")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void makeTextFailTest() throws Exception {
            TextCreateForm form = new TextCreateForm("", "", "", "", null);
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", form)).andReturn();

            //리다이렉트 되지않고 다시 글 생성 페이지로 왔는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/makeText");

            //글이 생성되지 않음을 확인
            assertThat(textRepository.findAll().size()).isEqualTo(2);
        }



    }

    @Nested
    @DisplayName("글 수정 - /edit/{id}")
    public class editTextTest {
        private String URL = "/edit/" ;

        @BeforeEach
        public void init(){
            baseInit();
            Long textId = text1.getId();
            URL += textId;
        }

        @Test
        @DisplayName("다른 사람의 글을 수정하려 시도할 경우")
        @WithUserDetails(value = "id456", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editOthersText() throws Exception {

            MvcResult mvcResult = mvcBuilder("get", URL);
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("존재하지 않는 글을 수정하려 시도할 경우")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editNotExistText() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", "/edit/-1");
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("뷰 테스트")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextViewTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/makeText");
        }

        @Test
        @DisplayName("모델값 테스트")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextModelTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);
            TextUpdateForm textUpdateForm = (TextUpdateForm) mvcResult.getModelAndView().getModel().get("text");
            assertThat(textUpdateForm.getContent()).isEqualTo(text1.getContent());
            assertThat(textUpdateForm.getTitle()).isEqualTo(text1.getTitle());
          //  assertThat(textUpdateForm.getFile().getOriginalFilename()).isEqualTo(text1.getFileName());

            String hashtags = hashtagService.mergeHashtag(textHashtagRepository.findHashtagsByText(text1));
            assertThat(textUpdateForm.getHashtags()).isEqualTo(hashtags);
        }

        @Test
        @DisplayName("글 수정 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextSuccessTest() throws Exception {

            TextUpdateForm newForm = new TextUpdateForm("newTitle1", "newContent1", "11,22,33,44", null);

            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", newForm)).andReturn();
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text1.getId());

            Text text = textRepository.findById(text1.getId()).orElseThrow();

            assertThat(text.getTitle()).isEqualTo("newTitle1");
            assertThat(text.getContent()).isEqualTo("newContent1");
//            assertThat(text.getFileName()).isEqualTo(null);
            String hashtags = hashtagService.mergeHashtag(textHashtagRepository.findHashtagsByText(text));
            assertThat(hashtags).isEqualTo("11,22,33,44");
        }

        @Test
        @DisplayName("글 수정 실패")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextFailTest() throws Exception {
            TextUpdateForm newForm = new TextUpdateForm("", "", "", null);

            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", newForm)).andReturn();

            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(URL);

            Text text = textRepository.findById(text1.getId()).orElseThrow();
            assertThat(text.equals(text1)).isTrue();
        }
    }

    @Nested
    @DisplayName("글 삭제 - POST delete/{id}")
    public class deleteText{
        private String URL = "/delete/" ;

        @BeforeEach
        public void init(){
            baseInit();
            Long textId = text1.getId();
            URL += textId;
        }

        @Test
        @DisplayName("다른 사람의 글을 삭제하려 시도할 경우")
        @WithUserDetails(value = "id456", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editOthersText() throws Exception {

        }

        @Test
        @DisplayName("존재하지 않는 글을 삭제하려 시도할 경우")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editNotExistText() {

        }

        @Test
        @DisplayName("글 삭제 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void deleteTextSuccessTest() {

        }
    }

    @Nested
    @DisplayName("댓글 추가 - POST comments/new")
    public class commentsNew{
        private String URL = "/delete/" ;

        @BeforeEach
        public void init(){
            baseInit();
            Long textId = text1.getId();
            URL += textId;
        }

        @Test
        @DisplayName("댓글 생성 성공")
        public void commentsNewSuccessTest(){

        }

        @Test
        @DisplayName("댓글 생성 실패")
        public void commentsNewFailTest(){

        }
    }

    @Nested
    @DisplayName("북마크 추가 - bookmarks/new")
    public class bookmarkNew{


        private String URL = "/bookmarks/new/" ;

        @BeforeEach
        public void init(){
            baseInit();

            Bookmark bookmark = new Bookmark(text1, user1);
            bookmarkRepository.save(bookmark);
        }

        @Test
        @DisplayName("북마크 추가 성공")
        public void bookmarkNewSuccessTest(){

        }

        @Test
        @DisplayName("북마크가 이미 추가된 상태에서 추가 요청을 보낸경우")
        public void dupBookmarkNewTest(){

        }
    }

    @Nested
    @DisplayName("북마크 추가 - bookmarks/delete")
    public class bookmarkDelete{


        private String URL = "/bookmarks/delete/" ;

        @BeforeEach
        public void init(){
            baseInit();

            Bookmark bookmark = new Bookmark(text1, user1);
            bookmarkRepository.save(bookmark);
        }

        @Test
        @DisplayName("북마크가 추가되지 않은 상태에서 삭제 요청")
        public void dupBookmarkDeleteTest(){

        }

        @Test
        @DisplayName("북마크 삭제 성공")
        public void bookmarkDeleteSuccessTest(){

        }


    }



    private MvcResult mvcBuilder(String method, String URL) throws  Exception{
        if (method.equals("get")) {
            return mockMvc.perform(get(URL)).andReturn();
        } else if(method.equals("post")){
            return mockMvc.perform(get(URL)).andReturn();
        }
        return null;
    }

}