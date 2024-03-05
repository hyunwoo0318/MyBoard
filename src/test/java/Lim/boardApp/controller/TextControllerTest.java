package Lim.boardApp.controller;

import Lim.boardApp.ObjectValue.PageConst;
import Lim.boardApp.ObjectValue.TextType;
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
import Lim.boardApp.service.TextService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ContextConfiguration
@Sql(scripts = {"classpath:db/initUser.sql"})
class TextControllerTest {

    @Autowired
    private TextService textService;

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

    private Text text1, text2,text3, text4;

    private Hashtag h1,h2,h3;

    private void baseInit(){
        //board 2종류 저장
        soccer = new Board("soccer");
        basketBall = new Board("basketBall");
        boardRepository.saveAndFlush(soccer);
        boardRepository.saveAndFlush(basketBall);

        //initUser.sql에서 insert한 회원 정보 가져오기
        user1 = customerRepository.findByLoginId("id123").get();
        user2 = customerRepository.findByLoginId("id456").get();

        /**
         * text1 -> soccer, general
         * text2 -> soccer, article
         * text3 -> basketball, general
         * text4 -> basketball, article
         */
        text1 = Text.builder()
                .board(soccer)
                .title("title1")
                .content("content1")
                .textType(TextType.GENERAL)
                .customer(user1)
                .build();
        text2 = Text.builder()
                .board(soccer)
                .title("title2")
                .content("content2")
                .textType(TextType.ARTICLE)
                .customer(user1)
                .build();

        text3 = Text.builder()
                .board(basketBall)
                .title("title3")
                .content("content3")
                .textType(TextType.GENERAL)
                .customer(user1)
                .build();
        text4 = Text.builder()
                .board(basketBall)
                .title("title4")
                .content("content4")
                .textType(TextType.ARTICLE)
                .customer(user1)
                .build();

        textRepository.saveAllAndFlush(Arrays.asList(text1, text2, text3, text4));

        //hashtag h1,h2,h3 저장
        h1 = new Hashtag("h1");
        h2 = new Hashtag("h2");
        h3 = new Hashtag("h3");

        hashtagRepository.saveAllAndFlush(Arrays.asList(h1, h2, h3));
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

            PageForm expectPageForm = textService.pagingByAll(2, PageConst.PAGE_SIZE, PageConst.PAGE_BLOCK_SIZE, "전체",null);
            assertThat(expectPageForm.equals(mvcResult.getModelAndView().getModel().get("pageForm"))).isTrue();
        }



    }

    @Test
    @DisplayName("글 검색 테스트 - /search")
    public void searchTextListTest() throws Exception{

    }


    @Nested
    @DisplayName("특정 글 조회 - /show/{id}")
    public class showText {

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

            Bookmark bookmark = new Bookmark(text1, user1);
            bookmarkRepository.saveAndFlush(bookmark);

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

            assertThat(model.get("isBookmarked")).isEqualTo(true);
            assertThat(model.get("commentCnt")).isEqualTo(3);

            ArrayList<Comment> commentList = (ArrayList<Comment>) model.get("commentList");
            List<String> commentContentList = commentList.stream().map(c -> c.getContent()).collect(Collectors.toList());
            assertThat(commentContentList.size()).isEqualTo(2);
            assertThat(commentContentList).contains("comment1", "comment3");
        }



    }

    @Nested
    @DisplayName("글 작성 - /new - 완료")
    public class makeText {

        private String URL = "/new";
        private int prevSize = 0;

        @BeforeEach
        public void init(){
            baseInit();
            prevSize = textRepository.findAll().size();
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
            TextCreateForm form = new TextCreateForm("title4", "content4", "h1,h2,h3", "soccer");
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", form)).andReturn();

            Text text4 = textRepository.findByTitle("title4").get(0);

            //생성이 완료하여 게시판 해당 글 조회 URL로 리다이렉트 되는지 검증
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text4.getId());

            //전달된 form대로 글이 잘 생성되었는지 확인
            assertThat(text4.getTitle()).isEqualTo("title4");
            assertThat(text4.getContent()).isEqualTo("content4");
            assertThat(text4.getBoard().getName()).isEqualTo("soccer");
            assertThat(text4.getCustomer().getId()).isEqualTo(user1.getId());

            //해당 글에 해시태그가 정상적으로 추가되었는지 확인
            List<String> hashtagNameList = textHashtagRepository.findHashtagsByText(text4).stream()
                    .map(h -> h.getName())
                    .collect(Collectors.toList());
            assertThat(hashtagNameList.containsAll(Arrays.asList("h1", "h2", "h3")));

            //글의 정상적으로 저장 -> 글의 개수가 1개 증가했는지 확인
            assertThat(textRepository.findAll().size()).isEqualTo(prevSize + 1);
        }

        @Test
        @DisplayName("글 생성 실패 - DTO Validation")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void makeTextFailWrongDto() throws Exception {
            TextCreateForm form = new TextCreateForm("", "", "", "");
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", form)).andReturn();

            //리다이렉트 되지않고 다시 글 생성 페이지로 왔는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("board/makeText");

            //글이 생성되지 않음을 확인
            assertThat(textRepository.findAll().size()).isEqualTo(prevSize);
        }



    }

    @Nested
    @DisplayName("글 수정 - /edit/{id} - 완료")
    public class editText {
        private String URL = "/edit/" ;

        @BeforeEach
        public void init(){
            baseInit();
            Long textId = text1.getId();
            URL += textId;

            TextHashtag textHashtag1 = new TextHashtag(text1, h1);
            TextHashtag textHashtag2 = new TextHashtag(text1, h2);
            textHashtagRepository.saveAllAndFlush(Arrays.asList(textHashtag1, textHashtag2));
        }

        @Test
        @DisplayName("다른 사람의 글을 수정하려 시도할 경우")
        @WithUserDetails(value = "id456", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editOthersText() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", URL);
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(403);
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

            String hashtags = textService.mergeHashtag(textHashtagRepository.findHashtagsByText(text1));
            assertThat(textUpdateForm.getHashtags()).isEqualTo(hashtags);
        }

        @Test
        @DisplayName("글 수정 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextSuccessTest() throws Exception {

            TextUpdateForm newForm = new TextUpdateForm("newTitle1", "newContent1", "11,22,33,44");

            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", newForm)).andReturn();
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text1.getId());

            Text text = textRepository.findById(text1.getId()).get();

            assertThat(text.getTitle()).isEqualTo("newTitle1");
            assertThat(text.getContent()).isEqualTo("newContent1");
            String hashtags = textService.mergeHashtag(textHashtagRepository.findHashtagsByText(text));
            assertThat(hashtags).isEqualTo("11,22,33,44");
        }

        @Test
        @DisplayName("글 수정 실패 - DTO Validation")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextFailWrongDto() throws Exception {
            TextUpdateForm newForm = new TextUpdateForm("", "", "");

            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("text", newForm)).andReturn();

            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(URL);

            Text text = textRepository.findById(text1.getId()).orElseThrow();
            assertThat(text.equals(text1)).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 글을 수정하려 시도할 경우")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextFailNotExistText() throws Exception {
            MvcResult mvcResult = mvcBuilder("get", "/edit/-1");
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("글 삭제 - delete/{id} - 완료")
    public class deleteText{
        private String URL = "/delete/" ;
        private int prevSize = 0;

        @BeforeEach
        public void init(){
            baseInit();
            Long textId = text1.getId();
            URL += textId;

            prevSize = textRepository.findAll().size();
        }

        @Test
        @DisplayName("다른 사람의 글을 삭제하려 시도할 경우")
        @WithUserDetails(value = "id456", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editOthersText() throws Exception {
            MvcResult mvcResult =  mvcBuilder("post", URL);
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("글 삭제 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void deleteTextSuccessTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL);

            //삭제 성공시 홈화면으로 리다이렉트 되는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/");

            //삭제한 글이 더이상 존재하지 않음을 확인
            Optional<Text> textOptional = textRepository.findById(text1.getId());
            assertThat(textOptional.isEmpty()).isTrue();

            //글의 개수가 1개 줄어듬을 확인
            assertThat(textRepository.findAll().size()).isEqualTo(prevSize - 1);
        }

        @Test
        @DisplayName("글 삭제 실패 - 존재하지 않는 글을 삭제하려 시도한경우")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void editTextFailNotExist() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", "/delete/-1");
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);

            //글의 개수에 변동이 없음을 확인
            assertThat(textRepository.findAll().size()).isEqualTo(prevSize);
        }
    }

    @Nested
    @DisplayName("댓글 추가 - POST comments/new - 완료")
    public class commentsNew{
        private String URL = "/comments/new" ;
        private Long textId;
        private Comment comment1, comment2, comment3;

        private int prevSize = 0;

        @BeforeEach
        public void init(){
            baseInit();
            textId = text1.getId();

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

            URL = "/text/" + textId + URL;

            prevSize = commentRepository.findAll().size();
        }

        @Test
        @DisplayName("댓글 생성 성공 - 댓글 작성")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void commentsNewSuccess() throws Exception {

            int prevTextCommentCnt = commentRepository.queryCommentByText(textId).size();
            String newCommentContent = "new Comment1";
            CommentForm commentForm = new CommentForm(newCommentContent, null);
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("commentForm", commentForm)).andReturn();

            //해당 글 조회로 리다이렉트 되는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + textId);

            //해당 글에 댓글이 정상적으로 추가되었는지 확인
            List<Comment> textCommentList = commentRepository.queryCommentByText(textId);
            assertThat(textCommentList.size()).isEqualTo(prevTextCommentCnt + 1);

            //추가된 댓글의 내용 확인
            List<Comment> newCommentList = textCommentList.stream()
                    .filter(c -> c.getContent().equals(newCommentContent))
                    .collect(Collectors.toList());

            assertThat(newCommentList.size()).isEqualTo(1);
            Comment newComment = newCommentList.get(0);
            assertThat(newComment.getContent()).isEqualTo(newCommentContent);
            assertThat(newComment.getText().getId()).isEqualTo(textId);
            assertThat(newComment.getParent()).isNull();
            assertThat(newComment.getChildCommentList()).usingRecursiveComparison().isEqualTo(new ArrayList<>());
            assertThat(newComment.getCustomer().getId()).isEqualTo(user1.getId());

            //댓글 전체의 개수가 1개 증가함을 확인
            assertThat(commentRepository.findCommentsByCustomer(user1.getLoginId()).size()).isEqualTo(prevSize + 1);
        }

        @Test
        @DisplayName("댓글 생성 성공 - 대댓글 작성")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void commentsHaveParentNewSuccess() throws Exception {
            int prevTextCommentCnt = commentRepository.queryCommentByText(textId).size();
            String newCommentContent = "new Comment1";
            CommentForm commentForm = new CommentForm(newCommentContent, comment3.getId());
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("commentForm", commentForm)).andReturn();

            //해당 글 조회로 리다이렉트 되는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + textId);

            //해당 글에 댓글이 정상적으로 추가되었는지 확인
            List<Comment> textCommentList = commentRepository.queryCommentByText(textId);
            assertThat(textCommentList.size()).isEqualTo(prevTextCommentCnt + 1);

            //추가된 댓글의 내용 확인
            List<Comment> newCommentList = textCommentList.stream()
                    .filter(c -> c.getContent().equals(newCommentContent))
                    .collect(Collectors.toList());

            assertThat(newCommentList.size()).isEqualTo(1);
            Comment newComment = newCommentList.get(0);
            assertThat(newComment.getContent()).isEqualTo(newCommentContent);
            assertThat(newComment.getText().getId()).isEqualTo(textId);
            assertThat(newComment.getCustomer().getId()).isEqualTo(user1.getId());

            /**
             * comment3
             *      ㄴ newComment
             *      구조가 잘 생성되었는지 확인
             */

            assertThat(newComment.getParent().getId()).isEqualTo(comment3.getId());
            assertThat(comment3.getChildCommentList())
                    .usingRecursiveComparison().isEqualTo(Arrays.asList(newComment));

            //댓글 전체의 개수가 1개 증가함을 확인
            assertThat(commentRepository.findAll().size()).isEqualTo(prevSize + 1);
        }

        @Test
        @DisplayName("댓글 생성 실패 - DTO Validation")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void commentsNewFailWrongDto() throws Exception {
            CommentForm commentForm = new CommentForm("", null);
            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("commentForm", commentForm)).andReturn();

            //해당 글 조회로 리다이렉트되는지 검증
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + textId);

            //댓글 전체의 개수에 변동이 없음을 확인
            assertThat(commentRepository.findAll().size()).isEqualTo(prevSize);
        }

        @Test
        @DisplayName("댓글 생성 실패 - 존재하지 않는 textId입력")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void commentsNewFailNotExistTextId() throws Exception {
            CommentForm commentForm = new CommentForm("newComment", comment1.getId());

            MvcResult mvcResult = mockMvc.perform(post("/text/-1/comments/new").flashAttr("commentForm", commentForm)).andReturn();

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);

            //댓글 전체의 개수에 변동이 없음을 확인
            assertThat(commentRepository.findAll().size()).isEqualTo(prevSize);
        }

        @Test
        @DisplayName("댓글 생성 실패 - 존재하지 않는 customerId입력")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void commentsNewFailNotExistCustomerId() throws Exception {
            CommentForm commentForm = new CommentForm("newComment",-13L);

            MvcResult mvcResult = mockMvc.perform(post(URL).flashAttr("commentForm", commentForm)).andReturn();

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);

            //댓글 전체의 개수에 변동이 없음을 확인
            assertThat(commentRepository.findAll().size()).isEqualTo(prevSize);
        }
    }

    @Nested
    @DisplayName("북마크 추가 - bookmarks/new - 완료")
    public class bookmarkNew{


        private String URL = "/bookmarks/new" ;

        @BeforeEach
        public void init(){
            baseInit();

            Bookmark bookmark = new Bookmark(text1, user1);
            bookmarkRepository.save(bookmark);
        }

        @Test
        @DisplayName("북마크 추가 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void bookmarkNewSuccess() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=" + text2.getId());

            //글 조회 화면으로 리다이렉트 되었는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text2.getId());

            //북마크가 성공적으로 저장 되었는지 확인
            Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text2, user1);
            assertThat(bookmarkOptional.isPresent()).isTrue();
        }

        /**
         * 이미 북마크가 추가되어있는 경우에 추가 요청을 보내면 따로 처리를 하지않음.
         */
        @Test
        @DisplayName("북마크가 이미 추가된 상태에서 추가 요청을 보낸경우")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void dupBookmarkNewSuccess() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=" + text1.getId());

            //글 조회 화면으로 리다이렉트 되었는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text1.getId());

            //북마크가 성공적으로 저장 되었는지 확인
            Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text1, user1);
            assertThat(bookmarkOptional.isPresent()).isTrue();
        }

        @Test
        @DisplayName("북마크 추가 실패 - 존재하지 않는 textId입력")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void bookmarkNewFailNotExistTextId() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=-13" );

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("북마크 삭제 - bookmarks/delete - 완료")
    public class bookmarkDelete{


        private String URL = "/bookmarks/delete/" ;

        @BeforeEach
        public void init(){
            baseInit();

            Bookmark bookmark = new Bookmark(text1, user1);
            bookmarkRepository.save(bookmark);
        }

        @Test
        @DisplayName("북마크 삭제 성공")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void bookmarkDeleteSuccess() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=" + text1.getId());

            //글 조회 화면으로 리다이렉트 되었는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text1.getId());

            //북마크가 성공적으로 삭제되었는지 확인
            Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text1, user1);
            assertThat(bookmarkOptional.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("북마크가 추가되지 않은 상태에서 삭제 요청")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void dupBookmarkDeleteTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=" + text2.getId());

            //글 조회 화면으로 리다이렉트 되었는지 확인
            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo("/show/" + text2.getId());

            //북마크가 성공적으로 삭제되었는지 확인
            Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text2, user1);
            assertThat(bookmarkOptional.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("북마크 삭제 실패 - 존재하지 않는 textId입력")
        @WithUserDetails(value = "id123", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        public void bookmarkDeleteSuccessTest() throws Exception {
            MvcResult mvcResult = mvcBuilder("post", URL + "?textId=-13" );

            assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
        }


    }



    private MvcResult mvcBuilder(String method, String URL) throws  Exception{
        if (method.equals("get")) {
            return mockMvc.perform(get(URL)).andReturn();
        } else if(method.equals("post")){
            return mockMvc. perform(post(URL)).andReturn();
        }
        return null;
    }

}