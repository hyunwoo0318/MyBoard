package Lim.boardApp.service;

import Lim.boardApp.form.PageBlockForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PagingServiceTest {

    @Autowired
    private PagingService pagingService;

    @Test
    @DisplayName("일반적인 페이징 블록 계산")
    public void normalPaging(){
        int totalPages = 20;
        int pageNum = 3;

        PageBlockForm pageBlockForm = pagingService.findBlock(pageNum, totalPages);

        assertEquals(pageBlockForm.start, 1);
        assertEquals(pageBlockForm.end, 5);
    }

}