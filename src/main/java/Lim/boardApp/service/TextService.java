package Lim.boardApp.service;

import Lim.boardApp.PageConst;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.repository.TextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TextService {

    private final TextRepository textRepository;
    public int getLastPage(int pageSize){
        PageRequest pageRequest = PageRequest.ofSize(pageSize);
        Page<Text> pages = textRepository.findAll(pageRequest);
        int totalPages = pages.getTotalPages();
        int lastPage;
        if(totalPages == 0) lastPage = 0;
        else lastPage = totalPages -1; // 0부터 시작
        return lastPage;
    }
    public PageBlockForm findBlock(int page, int lastPages, int blockSize){

        int blockNo = page / blockSize;
        int start = blockNo * blockSize;
        int end;
        if(lastPages < start + blockSize-1) end = lastPages;
        else end = start + blockSize -1;
        return new PageBlockForm(start, end, end - start + 1);
    }

}
