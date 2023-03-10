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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TextServiceTest {

    @InjectMocks
    TextService textService;
    @Mock
    TextRepository textRepository;
    @Mock CustomerRepository customerRepository;
    @Mock TextHashtagRepository textHashtagRepository;


    private List<Text> textList;
    private List<Hashtag> hashtagList;
    private List<TextHashtag> textHashtagList;

    @BeforeEach
    public void init() {
        /**
         * t0 - h1,h2,h3
         * t1 - h2,h3
         * t2 - h0, h3
         * t3 - h0,h1,h2,h3
         * t4 - h3
         */
        textList = addText(20);
        hashtagList = addHashTags(20);
        textHashtagList = initForHashtag();
    }

    @Test
    @DisplayName("makePageFormTest - 글이 존재하지 않을때")
    public void makePageFormWhenNoText(){
        int page = 0;
        int pageSize = 3;
        int blockSize = 5;

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> pages = new PageImpl<>(new ArrayList<>(),pageRequest,0);

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

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        int start = (int)pageRequest.getOffset();
        int end = (int)((start + pageSize > textList.size()) ? textList.size() : start + pageSize);
        Page<Text> pages = new PageImpl<>(textList.subList(start,end), pageRequest, textList.size());
        List<Text> resultList = textList.subList(9, 12);

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(3,5,3,3,6,resultList,false,false));
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

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        int start = (int)pageRequest.getOffset();
        int end = (int)((start + pageSize > textList.size()) ? textList.size() : start + pageSize);
        Page<Text> pages = new PageImpl<>(textList.subList(start,end), pageRequest, textList.size());
        List<Text> resultList = textList.subList(0, 3);


        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(0, 2, 3,0, 6, resultList, false, true));
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

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        int start = (int)pageRequest.getOffset();
        int end = (int)((start + pageSize > textList.size()) ? textList.size() : start + pageSize);
        Page<Text> pages = new PageImpl<>(textList.subList(start,end), pageRequest, textList.size());
        List<Text> resultList = textList.subList(18,20);

        PageForm result = textService.makePageForm(pages, page, blockSize);

        assertThat(result).isEqualTo(new PageForm(6, 6, 1,6, 6, resultList, true, false));
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
    @DisplayName("textCreateForm을 입력받아 정상적으로 text를 만드는 경우")
    public void createTextFromTextCreateForm(){
        //title, content 생성
        TextCreateForm form = new TextCreateForm("title123", "content123");

        //hashtag 5개 입력
        List<Hashtag> hashtags = hashtagList.subList(0, 6);

        //작성자 정보 입력
        Customer customer = new Customer("id123123","pw123123", "hy", 23, "USER", "hy@naver.com");
        customer.setId(1L);

        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));

        Text text = textService.createText(customer.getId(), form,hashtags);

        assertThat(text.getTitle()).isEqualTo("title123");
        assertThat(text.getContent()).isEqualTo("content123");
        assertThat(text.getCustomer()).isEqualTo(customer);
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
        Text prevText = new Text("prevContent", "prevTitle", customer);
        prevText.setId(1L);
        Text afterText = new Text("afterContent", "afterTitle", customer);

        List<Hashtag> prevHashtagList = new ArrayList<>();
        List<Hashtag> resultHashtagList = new ArrayList<>();

        for(int i=1;i<=8;i++){
            Hashtag h = new Hashtag("h" + i);
            if(i <3){
                prevHashtagList.add(h);
            }
            else if(i >= 6){
                prevHashtagList.add(h);
                resultHashtagList.add(h);
            }
            else{
                resultHashtagList.add(h);
            }
        }
        TextUpdateForm form = new TextUpdateForm(afterText);

        given(textRepository.findById(1L)).willReturn(Optional.of(prevText));

        Text result = textService.updateText(prevText.getId(), form,resultHashtagList);

        assertThat(result.getId()).isEqualTo(prevText.getId());
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getTitle()).isEqualTo(afterText.getTitle());
        assertThat(result.getContent()).isEqualTo(afterText.getContent());
    }




    private List<Text> addText(int num){
        Customer customer = new Customer();
        List<Text> ret = new ArrayList<>();
        for(int i=0;i<num;i++){
            Text text = new Text("content" + i, "title" + i, customer);
            ret.add(text);
        }
        return ret;
    }

    private List<Hashtag> addHashTags(int num){
        List<Hashtag> ret = new ArrayList<>();
        for(int i=1;i<=num;i++){
            String name = "h" + i;
            Hashtag hashtag = new Hashtag(name);
            ret.add(hashtag);
        }
        return ret;
    }

    private List<TextHashtag> initForHashtag(){
        /**
         * t0 - h1,h2,h3
         * t1 - h2,h3
         * t2 - h0, h3
         * t3 - h0,h1,h2,h3
         * t4 - h3
         */

        List<TextHashtag> ret = new ArrayList<>();
        TextHashtag textHashtag;

        //t0
        textHashtag = new TextHashtag(textList.get(0), hashtagList.get(1)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(0), hashtagList.get(2)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(0), hashtagList.get(3)); ret.add(textHashtag);

        //t1
        textHashtag = new TextHashtag(textList.get(1), hashtagList.get(2)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(1), hashtagList.get(3)); ret.add(textHashtag);

        //t2
        textHashtag = new TextHashtag(textList.get(2), hashtagList.get(0)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(2), hashtagList.get(3)); ret.add(textHashtag);

        //t3
        textHashtag = new TextHashtag(textList.get(3), hashtagList.get(0)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(3), hashtagList.get(1)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(3), hashtagList.get(2)); ret.add(textHashtag);
        textHashtag = new TextHashtag(textList.get(3), hashtagList.get(3)); ret.add(textHashtag);

        //t4
        textHashtag = new TextHashtag(textList.get(3), hashtagList.get(3)); ret.add(textHashtag);
        return ret;
    }

}