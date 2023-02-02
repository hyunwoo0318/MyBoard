package Lim.boardApp.service;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.TextRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TextServiceTest {

    @Autowired TextService textService;
    @Autowired TextRepository textRepository;
    @Autowired CustomerRepository customerRepository;

    @Test
    @DisplayName("페이지가 2개이상 있을때의 마지막 페이지 계산")
    public void calcLastPageWhen2orMorePage(){
        /**
         * text개수 = 6
         * pageSize = 2
         * lastPage -> 2
         */

        addText(6);

        int pageSize = 2;


        int lastPage = textService.getLastPage(pageSize);

        Assertions.assertThat(lastPage).isEqualTo(2);
    }

    @Test
    @DisplayName("페이지가 딱 1개 있을때의 마지막 페이지 계산")
    public void calcLastPageWhenOnly1Page(){

        /**
         * text개수 = 2
         * pageSize = 2
         * lastPage -> 0
         */
        addText(2);

        int pageSize = 2;

        int lastPage = textService.getLastPage(pageSize);

        assertThat(lastPage).isEqualTo(0);
    }

    @Test
    @DisplayName("페이지가 하나도 없을때의 마지막 페이지 계산")
    public void calcLastPageWhenNoPage(){
        /**
         * text개수 = 0
         * pageSize = 2
         * lastPage = 0
         */

        int pageSize = 2;
        int lastPage = textService.getLastPage(pageSize);

        assertThat(lastPage).isEqualTo(0);
    }

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

    private void addText(int num){
        Customer customer = new Customer();
        customerRepository.save(customer);
        for(int i=0;i<num;i++){
            Text text = new Text("content" + i, "title" + i, customer);
            textRepository.save(text);
        }
    }

}