package Lim.boardApp.repository;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TextHashtagRepositoryTest {

    @Autowired TextHashtagRepository textHashtagRepository;
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
            textHashtagRepository.save(new TextHashtag(text,h));
        }

        List<Hashtag> result = textHashtagRepository.findHashtagsByText(text);

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
                textHashtagRepository.save(new TextHashtag(text,h1));
                resultTextList.add(text);
            }
        }


        List<Text> result = textHashtagRepository.findTextsByHashtag(h1);

        assertThat(result.size()).isEqualTo(4);
        assertThat(result).isEqualTo(resultTextList);
    }

    @Test
    @DisplayName("deleteByText 테스트")
    public void deleteByTextTest(){
        Customer customer = new Customer("id123", "pw123", "name123", 23, "USER");
        customerRepository.save(customer);

        Text text1 = new Text("content123", "title123", customer);
        Text text2 = new Text("content456", "title456", customer);
        textRepository.save(text1);
        textRepository.save(text2);

        for(int i=1;i<=10;i++){
            Hashtag hashtag = new Hashtag("h" + i);
            hashtagRepository.save(hashtag);
            if(i<=5){
                textHashtagRepository.save(new TextHashtag(text1, hashtag));
            }else{
                textHashtagRepository.save(new TextHashtag(text2, hashtag));
            }
        }

        textHashtagRepository.deleteByText(text1);

        assertThat(textHashtagRepository.findAll().size()).isEqualTo(5);
        assertThat(textHashtagRepository.findAllByText(text1).size()).isEqualTo(0);
        assertThat(textHashtagRepository.findAllByText(text2).size()).isEqualTo(5);
    }

}