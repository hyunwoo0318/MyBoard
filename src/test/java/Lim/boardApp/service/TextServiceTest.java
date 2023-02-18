package Lim.boardApp.service;

import Lim.boardApp.domain.*;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TextServiceTest {

    @Autowired TextService textService;
    @Autowired TextRepository textRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired
    private HashtagRepository hashtagRepository;
    @Autowired
    private TextHashtagRepository textHashtagRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("makePageFormTest - 글이 존재하지 않을때")
    public void makePageFormWhenNoText(){
        int page = 0;
        int pageSize = 3;
        int blockSize = 5;

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> pages = textRepository.findAll(pageRequest);

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(0, 0, 1, 0, 0, new ArrayList<>(), true, true));
    }

    @Test
    @DisplayName("makePageFormTest - 글이 여러개 존재하는경우")
    public void makePageForWhenSomeTexts(){
        /**
         * 3번째 페이지 - text9, text10, text11
         */

        int page = 3;
        int pageSize = 3;
        int blockSize = 3;

        addText(20);

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> pages = textRepository.findAll(pageRequest);
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("title9").get(0));
        textList.add(textRepository.findByTitle("title10").get(0));
        textList.add(textRepository.findByTitle("title11").get(0));

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(3,5,3,3,6,textList,false,false));
    }

    @Test
    @DisplayName("makePageForm - 여러개의 text가 있을때 첫 페이지")
    public void makePageFormWhenFirstPage(){

        /**
         * 첫번째 페이지 text0, text1, text2
         */
        int page = 0;
        int pageSize = 3;
        int blockSize = 3;

        addText(20);

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> pages = textRepository.findAll(pageRequest);
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("title0").get(0));
        textList.add(textRepository.findByTitle("title1").get(0));
        textList.add(textRepository.findByTitle("title2").get(0));

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(0, 2, 3,0, 6, textList, false, true));
    }

    @Test
    @DisplayName("makePageForm - 여러개의 text가 있을때 마지막 페이지")
    public void makePageFormWhenLastPage(){

        /**
         * 마지막 페이지 text18,text19
         */
        int page = 6;
        int pageSize = 3;
        int blockSize = 3;

        addText(20);

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> pages = textRepository.findAll(pageRequest);
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("title18").get(0));
        textList.add(textRepository.findByTitle("title19").get(0));

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(6, 6, 1,6, 6, textList, true, false));
    }

    @Test
    @DisplayName("pagingBySearch - type이 주어지지 않았을때")
    public void pagingBySearchNoType(){

        int page = 0;
        int pageSize = 3;
        int blockSize = 3;
        String searchKey = "title";
        String type="";

        PageForm pageForm = textService.pagingBySearch(page, pageSize, blockSize, searchKey, type);
        assertThat(pageForm).isNull();
    }

    @Test
    @DisplayName("pagingBySearch - searchKey가 주어지지 않았을때")
    public void pagingBySearchNoSearchKey(){
        /**
         * searchKey가 주어지지 않으면 전체를 보여주는것과 같은 결과를 가짐
         */

        addText(20);
        int page = 0;
        int pageSize = 3;
        int blockSize = 3;
        String searchKey = "";

        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("title0").get(0));
        textList.add(textRepository.findByTitle("title1").get(0));
        textList.add(textRepository.findByTitle("title2").get(0));

        PageForm expectForm = new PageForm(0, 2, 3, 0, 6, textList, false, true);

        PageForm formAll = textService.pagingBySearch(page, pageSize, blockSize, searchKey, "all");
        PageForm formTitle = textService.pagingBySearch(page, pageSize, blockSize, searchKey, "title");
        PageForm formHashtag = textService.pagingBySearch(page, pageSize, blockSize, searchKey, "hashtag");
        PageForm formContent = textService.pagingBySearch(page, pageSize, blockSize, searchKey, "content");

        assertThat(formAll).isEqualTo(expectForm);
        assertThat(formTitle).isEqualTo(expectForm);
        assertThat(formHashtag).isEqualTo(expectForm);
        assertThat(formContent).isEqualTo(expectForm);
    }

 //   @Test
   // @DisplayName("pagingBySearch - hashtag")
    public void pagingBySearchByHashtag(){
        /**
         * t0 - h1,h2,h3
         * t1 - h2,h3
         * t2 - h0, h3
         * t3 - h0,h1,h2,h3
         * t4 - h3
         */
       initForHashtag();

        PageForm formH1 = textService.pagingBySearch(0, 3, 3, "h1", "hashtag");
        PageForm formH4 = textService.pagingBySearch(0, 3, 3, "h4", "hashtag");
//
//        List<Text> textList = Arrays.asList()
//        assertThat()

    }

    @Test
    @DisplayName("textCreateForm을 입력받아 정상적으로 text를 만드는 경우")
    public void createTextFromTextCreateForm(){
        //미리 입력된 해시태그 개수 확인
        int prevSize = hashtagRepository.findAll().size();

        //title, content 생성
        TextCreateForm form = new TextCreateForm("title123", "content123");

        //hashtag 5개 입력
        addHashTags(5);
        List<Hashtag> hashtags = hashtagRepository.findAll();

        //작성자 정보 입력
        Customer customer = new Customer();
        customerRepository.save(customer);

        Text text = textService.createText(customer.getId(), form,hashtags);

        assertThat(text.getTitle()).isEqualTo("title123");
        assertThat(text.getContent()).isEqualTo("content123");
        assertThat(text.getCustomer()).isEqualTo(customer);
        assertThat(textHashtagRepository.findHashtagsByText(text).size()).isEqualTo(5 + prevSize);
        assertThat(textHashtagRepository.findHashtagsByText(text)).isEqualTo(hashtags);
    }

    @Test
    @DisplayName("updateForm을 이용한 글 수정")
    public void updateTextFromUpdateForm(){
        /**
         * Before)
         * text - content : prevContent, title : prevTitle, customer : customer
         *      - Hashtag : h1,h2,h3,h4,h5
         * After)
         * text - content : afterContent, title : afterTitle, customer : customer
         *      - Hashtag : h3,h4,h5,h6,h7,h8
         */
        Customer customer = new Customer();
        customerRepository.save(customer);
        Text prevText = new Text("prevContent", "prevTitle", customer);
        textRepository.save(prevText);
        Text afterText = new Text("afterContent", "afterTitle", customer);
        textRepository.save(afterText);

        List<Hashtag> resultHashtagList = new ArrayList<>();

        for(int i=1;i<=8;i++){
            Hashtag h = new Hashtag("h" + i);
            hashtagRepository.save(h);
            if(i <3){
                textHashtagRepository.save(new TextHashtag(prevText, h));
            }
            else if(i >= 6){
                textHashtagRepository.save(new TextHashtag(afterText, h));
                resultHashtagList.add(h);
            }
            else{
                textHashtagRepository.save(new TextHashtag(prevText, h));
                textHashtagRepository.save(new TextHashtag(afterText, h));
                resultHashtagList.add(h);
            }
        }

        TextUpdateForm form = new TextUpdateForm(afterText);

        Text result = textService.updateText(prevText.getId(), form,resultHashtagList);

        assertThat(result.getId()).isEqualTo(prevText.getId());
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getTitle()).isEqualTo(afterText.getTitle());
        assertThat(result.getContent()).isEqualTo(afterText.getContent());

        assertThat(textHashtagRepository.findHashtagsByText(result).size()).isEqualTo(6);
        assertThat(textHashtagRepository.findHashtagsByText(result)).isEqualTo(resultHashtagList);
    }

    @Test
    @DisplayName("findTextTest")
    public void findTextTest(){
        addText(3);

        List<Text> text1 = textService.findText("title1");
        List<Text> text2 = textService.findText("noTitle");

        assertThat(text1.size()).isEqualTo(1);
        for(int i=0;i<text1.size();i++){
            Text text = text1.get(i);
            assertThat(text.getContent()).isEqualTo("content1");
        }
        assertThat(text2.size()).isEqualTo(0);
        assertThat(text2).isEqualTo(new ArrayList<>());
    }

    @Test
    @DisplayName("deleteTextTest")
    public void deleteTextTest(){

        //글 작성
        addText(3);
        List<Text> textList = textRepository.findAll();
        Text text = textList.get(0);
        addHashTags(3);
        List<Hashtag> hashtagList = hashtagRepository.findAll();
        for (Hashtag hashtag : hashtagList) {
            TextHashtag textHashtag = new TextHashtag(text, hashtag);
            textHashtagRepository.save(textHashtag);

        }

        //댓글작성
        Customer customer1 = text.getCustomer();
        Customer customer2 = new Customer("id123", "pw123", "c2", 23, "USER");
        customerRepository.save(customer2);
        commentRepository.save(new Comment(text, customer1, "comment1"));
        commentRepository.save(new Comment(text, customer1, "comment2"));
        commentRepository.save(new Comment(text, customer2, "comment11"));
        commentRepository.save(new Comment(text, customer2, "comment22"));

        textService.deleteText(text.getId());

        assertThat(textRepository.findById(text.getId())).isEmpty();
    }

    private List<Text> addText(int num){
        Customer customer = new Customer();
        customerRepository.save(customer);
        List<Text> ret = new ArrayList<>();
        for(int i=0;i<num;i++){
            Text text = new Text("content" + i, "title" + i, customer);
            textRepository.save(text);
            ret.add(text);
        }
        return ret;
    }

    private List<Hashtag> addHashTags(int num){
        List<Hashtag> ret = new ArrayList<>();
        for(int i=1;i<=num;i++){
            Hashtag hashtag = new Hashtag("h" + i);
            hashtagRepository.save(hashtag);
            ret.add(hashtag);
        }
        return ret;
    }

    private void initForHashtag(){
        /**
         * t0 - h1,h2,h3
         * t1 - h2,h3
         * t2 - h0, h3
         * t3 - h0,h1,h2,h3
         * t4 - h3
         */
        List<Text> textList = addText(5);
        List<Hashtag> hashtagList = addHashTags(5);

        //t0
        textHashtagRepository.save(new TextHashtag(textList.get(0),hashtagList.get(1)));
        textHashtagRepository.save(new TextHashtag(textList.get(0),hashtagList.get(2)));
        textHashtagRepository.save(new TextHashtag(textList.get(0),hashtagList.get(3)));
        //t1
        textHashtagRepository.save(new TextHashtag(textList.get(1),hashtagList.get(2)));
        textHashtagRepository.save(new TextHashtag(textList.get(1),hashtagList.get(3)));

        //t2
        textHashtagRepository.save(new TextHashtag(textList.get(2),hashtagList.get(0)));
        textHashtagRepository.save(new TextHashtag(textList.get(2),hashtagList.get(3)));

        //t3
        textHashtagRepository.save(new TextHashtag(textList.get(3),hashtagList.get(0)));
        textHashtagRepository.save(new TextHashtag(textList.get(3),hashtagList.get(1)));
        textHashtagRepository.save(new TextHashtag(textList.get(3),hashtagList.get(2)));
        textHashtagRepository.save(new TextHashtag(textList.get(3),hashtagList.get(3)));

        //t4
        textHashtagRepository.save(new TextHashtag(textList.get(3),hashtagList.get(3)));
    }

}