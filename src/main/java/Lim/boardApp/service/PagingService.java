package Lim.boardApp.service;

import Lim.boardApp.PageConst;
import Lim.boardApp.form.PageBlockForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagingService {

    public PageBlockForm findBlock(int page,int totalPages){
        int blockNo = page / PageConst.PAGE_BLOCK_LENGTH;
        int first = blockNo * PageConst.PAGE_BLOCK_LENGTH+1;
        int last = (blockNo + 1) * PageConst.PAGE_BLOCK_LENGTH;

        if(first <= 1) first = 1;
        if(last >= totalPages) last = totalPages;

        PageBlockForm form = PageBlockForm.builder()
                .start(first)
                .end(last)
                .length(last - first)
                .build();
        return form;
    }
}
