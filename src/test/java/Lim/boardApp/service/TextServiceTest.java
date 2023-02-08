package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.HashtagRepository;
import Lim.boardApp.repository.TextHashtagRepository;
import Lim.boardApp.repository.TextRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private void addText(int num){
        Customer customer = new Customer();
        customerRepository.save(customer);
        for(int i=0;i<num;i++){
            Text text = new Text("content" + i, "title" + i, customer);
            textRepository.save(text);
        }
    }

    private void addHashTags(int num){
        for(int i=1;i<=num;i++){
            hashtagRepository.save(new Hashtag("h" + i));
        }
    }

}