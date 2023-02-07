package Lim.boardApp.repository;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class TextHashtagRepositoryTest {

    @Autowired TextHashtagRepository repo;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TextRepository textRepository;
    @Autowired
    private HashtagRepository hashtagRepository;

    @Test
    @DisplayName("findHashtagsByText 테스트")
    public void findHashtagsByTextTest(){
        /**
         * text에 5개의 hashtag를 넣음
         */
        int prevSize = hashtagRepository.findAll().size();

        Text text = new Text();
        textRepository.save(text);

        for(int i=1;i<=5;i++){
            hashtagRepository.save(new Hashtag("h" + i));
        }
        List<Hashtag> hashtags = hashtagRepository.findAll();

        for(Hashtag h : hashtags){
            repo.save(new TextHashtag(text,h));
        }

        List<Hashtag> result = repo.findHashtagsByText(text);

        assertThat(result.size()).isEqualTo(5 + prevSize);
        assertThat(result).isEqualTo(hashtags);
    }

    @Test
    @DisplayName("findTextsByHashtag 테스트")
    public void findTextsByHashtagTest(){
        /**
         * text 1 ~ 10까지 존재
         * text 5,6,7,8에 hashtag h1이 존재
         */

        Customer c = new Customer();
        customerRepository.save(c);

        List<Text> textList = new ArrayList<>();
        Hashtag h1 = new Hashtag("h1");
        hashtagRepository.save(h1);
        List<Text> resultTextList = new ArrayList<>();

        for(int i=1;i<=10;i++){
            Text text = new Text("content","t" + i,c);
            textRepository.save(text);
            if(i>=5 && i<=8){
                repo.save(new TextHashtag(text,h1));
                resultTextList.add(text);
            }
        }


        List<Text> result = repo.findTextsByHashtag(h1);

        assertThat(result.size()).isEqualTo(4);
        assertThat(result).isEqualTo(resultTextList);
    }
}