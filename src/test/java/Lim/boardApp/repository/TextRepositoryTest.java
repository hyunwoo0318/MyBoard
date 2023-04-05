package Lim.boardApp.repository;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TextRepositoryTest {

    @Autowired TextRepository textRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    public void init(){
        Customer customer = new Customer();
        customerRepository.save(customer);

        for(int i=1;i<=30;i++){
            textRepository.save(new Text("c" + i, "t" + i, customer));
        }

    }



    @Test
    @DisplayName("제목/내용으로 글 검색")
    public void searchTextByContentTitleTest(){
        /**
         * '1'가 들어가는 글 검색 => t1,t10~t19, t21 총 12개
         *  pagesize = 3
         */

        PageRequest pr5 = PageRequest.of(3, 3); // t18, t19, t21
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("t18").get(0));
        textList.add(textRepository.findByTitle("t19").get(0));
        textList.add(textRepository.findByTitle("t21").get(0));

        Page<Text> page5 = textRepository.searchTextByContentTitle("1", pr5);

        assertThat(page5.getContent()).isEqualTo(textList);
        assertThat(page5.getTotalPages()).isEqualTo(4);
        assertThat(page5.getNumber()).isEqualTo(3);
        assertThat(page5.getNumberOfElements()).isEqualTo(3);
        assertThat(page5.isLast()).isTrue();
        assertThat(page5.isFirst()).isFalse();
    }

    @Test
    @DisplayName("제목으로 글 찾기 테스트")
    public void searchTextTitleTest(){
        /**
         * 1) 'c' -> 존재 x
         * 2) '1' -> t1,t10~t19,t21
         */

        PageRequest pr5 = PageRequest.of(3, 3); // t18, t19, t21
        PageRequest pr0 = PageRequest.of(0, 3);
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("t18").get(0));
        textList.add(textRepository.findByTitle("t19").get(0));
        textList.add(textRepository.findByTitle("t21").get(0));

        Page<Text> page5 = textRepository.searchTextByTitle("1", pr5);
        Page<Text> page0 = textRepository.searchTextByTitle("c",pr0);

        assertThat(page5.getContent()).isEqualTo(textList);
        assertThat(page5.getTotalPages()).isEqualTo(4);
        assertThat(page5.getNumber()).isEqualTo(3);
        assertThat(page5.getNumberOfElements()).isEqualTo(3);
        assertThat(page5.isLast()).isTrue();
        assertThat(page5.isFirst()).isFalse();

        assertThat(page0.getContent()).isEmpty();
        assertThat(page0.getTotalPages()).isEqualTo(0);
        assertThat(page0.isFirst()).isTrue();
        assertThat(page0.isLast()).isTrue();
        assertThat(page0.getNumberOfElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("내용으로 글 찾기 테스트")
    public void searchTextContentTest(){
        /**
         * 1) 't' -> 존재 x
         * 2) '1' -> c1,c10~c19,c21
         */

        PageRequest pr5 = PageRequest.of(3, 3); // t18, t19, t21
        PageRequest pr0 = PageRequest.of(0, 3);
        List<Text> textList = new ArrayList<>();
        textList.add(textRepository.findByTitle("t18").get(0));
        textList.add(textRepository.findByTitle("t19").get(0));
        textList.add(textRepository.findByTitle("t21").get(0));

        Page<Text> page5 = textRepository.searchTextByContent("1", pr5);
        Page<Text> page0 = textRepository.searchTextByContent("t",pr0);

        assertThat(page5.getContent()).isEqualTo(textList);
        assertThat(page5.getTotalPages()).isEqualTo(4);
        assertThat(page5.getNumber()).isEqualTo(3);
        assertThat(page5.getNumberOfElements()).isEqualTo(3);
        assertThat(page5.isLast()).isTrue();
        assertThat(page5.isFirst()).isFalse();

        assertThat(page0.getContent()).isEmpty();
        assertThat(page0.getTotalPages()).isEqualTo(0);
        assertThat(page0.isFirst()).isTrue();
        assertThat(page0.isLast()).isTrue();
        assertThat(page0.getNumberOfElements()).isEqualTo(0);
    }
}