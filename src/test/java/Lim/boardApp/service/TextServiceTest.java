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
    @DisplayName("일반적인 페이지 블록 계산")
    public void calcPageBlock(){
        /**
         * text개수 = 10
         * pageBlockSize = 3
         * lastPage = 9
         * 5번 페이지의 페이지 블록 - (3 ~ 5)
         * 7번 페이지의 페이지 블록 - (6 ~ 8)
         */

        addText(10);

        int pageBlockSize = 3;
        int lastPage = 9;

        PageBlockForm blockPage5 = textService.findBlock(5, lastPage,pageBlockSize);
        PageBlockForm blockPage7 = textService.findBlock(7, lastPage,pageBlockSize);

        assertThat(blockPage5).isEqualTo(new PageBlockForm(3,5,3));
        assertThat(blockPage7).isEqualTo(new PageBlockForm(6,8,3));
    }

    @Test
    @DisplayName("마지막 블록에 블록 사이즈보다 블록 개수가 적은경우")
    public void calcLastBlockLessThanBlockSize(){
        /**
         * text개수 = 10개
         * blockSize = 3
         * lastPage = 9
         * page 9 -> (9 ~ 9)
         */

        addText(10);

        int pageBlockSize = 3;
        int lastPage = 9;

        PageBlockForm blockPage9 = textService.findBlock(9, lastPage,pageBlockSize);

        assertThat(blockPage9).isEqualTo(new PageBlockForm(9,9,1));
    }

    @Test
    @DisplayName("첫번째 블록에 블록 사이즈보다 블록 개수가 적은경우")
    public void calcFirstBlockLessThanBlockSize(){
        /**
         * text개수 = 2개
         * blockSize = 3
         * lastPage = 1
         * page 1 -> (0 ~ 1)
         */

       addText(2);

        int pageBlockSize = 3;
        int lastPage = 1;

        PageBlockForm blockPage9 = textService.findBlock(1, lastPage,pageBlockSize);

        assertThat(blockPage9).isEqualTo(new PageBlockForm(0,1,2));
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
        form.setHashtags(hashtags);

        //작성자 정보 입력
        Customer customer = new Customer();
        customerRepository.save(customer);

        Text text = textService.createText(customer.getId(), form);

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
        form.setHashtags(resultHashtagList);

        Text result = textService.updateText(prevText.getId(), form);

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