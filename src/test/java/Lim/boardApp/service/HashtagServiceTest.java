package Lim.boardApp.service;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.repository.HashtagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService hashtagService;

    @Mock private HashtagRepository hashtagRepository;

    @Test
    @DisplayName("parseHashtag 테스트")
    public void parseHashtagTest(){
        /**
         * a,b,c,d,e,f,g를 입력
         **/

        String input = "a,b,c,d,e,f,g";
        List<String> resultInput = Arrays.asList("#a","#b","#c","#d","#e","#f","#g");

        List<Hashtag> hashtagList = hashtagService.parseHashtag(input);

        assertThat(hashtagList.size()).isEqualTo(7);
        assertThat(hashtagList.stream().map(m -> m.getName()).collect(Collectors.toList())).isEqualTo(resultInput);
    }

    @Test
    @DisplayName("mergeHashtag 테스트")
    public void mergeHashtagTest(){
        /**
         *  #a,#b,#c,#d,#e,#f,#g -> a,b,c,d,e,f,g
         */
        Hashtag a = new Hashtag("#a");
        Hashtag b = new Hashtag("#b");
        Hashtag c = new Hashtag("#c");
        Hashtag d = new Hashtag("#d");
        Hashtag e = new Hashtag("#e");
        Hashtag f = new Hashtag("#f");
        Hashtag g = new Hashtag("#g");

        List<Hashtag> hashtagList = Arrays.asList(a, b, c, d, e, f, g);
        String resultInput = "a,b,c,d,e,f,g";

        String result = hashtagService.mergeHashtag(hashtagList);
        assertThat(result.equals(resultInput)).isTrue();
    }
}