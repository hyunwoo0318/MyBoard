package Lim.boardApp.service;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.repository.HashtagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class HashtagServiceTest {

    @Autowired private HashtagService hashtagService;
    @Autowired private HashtagRepository hashtagRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    public void init(){
        em.clear();
    }

    @Test
    @DisplayName("parseHashtag 테스트")
    public void parseHashtagTest(){
        /**
         * a,b,c,d,e,f,g를 입력
         * a,b,c는 이미 있는 상황 가정
         */

        hashtagRepository.save(new Hashtag("#a"));
        hashtagRepository.save(new Hashtag("#b"));
        hashtagRepository.save(new Hashtag("#c"));

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
        hashtagRepository.save(new Hashtag("#a"));
        hashtagRepository.save(new Hashtag("#b"));
        hashtagRepository.save(new Hashtag("#c"));
        hashtagRepository.save(new Hashtag("#d"));
        hashtagRepository.save(new Hashtag("#e"));
        hashtagRepository.save(new Hashtag("#f"));
        hashtagRepository.save(new Hashtag("#g"));

        List<Hashtag> hashtagList = hashtagRepository.findAll();
        String resultInput = "a,b,c,d,e,f,g";

        String result = hashtagService.mergeHashtag(hashtagList);
        assertThat(result.equals(resultInput)).isTrue();
    }
}