package Lim.boardApp.service;

import Lim.boardApp.domain.*;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TextService {

    private final TextRepository textRepository;
    private final TextHashtagRepository textHashtagRepository;
    private final CustomerRepository customerRepository;
    private final HashtagRepository hashtagRepository;

    public PageForm pagingByAll(int page,int pageSize,int blockSize){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> findPage = textRepository.findAll(pageRequest);

        return makePageForm(findPage, page, blockSize);
    }

    public PageForm pagingBySearch(int page, int pageSize, int blockSize, String searchKey, String type){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        if(type.equals("all")){
            Page<Text> findPage = textRepository.searchTextByContentTitle(searchKey, pageRequest);
            return makePageForm(findPage, page, blockSize);
        } else if(type.equals("content")){
            Page<Text> findPage = textRepository.searchTextByContent(searchKey, pageRequest);
            return makePageForm(findPage, page, blockSize);
        } else if(type.equals("title")){
            Page<Text> findPage = textRepository.searchTextByTitle(searchKey, pageRequest);
            return makePageForm(findPage, page, blockSize);
        }else if(type.equals("hashtag")){
            if(!searchKey.isBlank()){
                searchKey = "#" + searchKey;
            }
            Hashtag hashtag = hashtagRepository.findByName(searchKey);
            Page<Text> findPage = textHashtagRepository.findTextsByHashtag(hashtag, pageRequest);
            return makePageForm(findPage, page, blockSize);
        } else  return null;
    }

    public PageForm makePageForm(Page<Text> findPage, int page, int blockSize){
        int lastPage = findPage.getTotalPages()-1;
        if(lastPage == -1){
            return new PageForm(0,0,1,0,0,new ArrayList<Text>(), true, true);
        }

        int blockNo = page / blockSize;
        int start = blockNo * blockSize;
        int end;
        if(lastPage < start + blockSize-1) end = lastPage;
        else end = start + blockSize -1;
        PageBlockForm pageBlockForm = new PageBlockForm(start, end, end - start + 1);

        PageForm pageForm = new PageForm(start,end, end-start + 1, page, lastPage, findPage.getContent(), findPage.isLast(), findPage.isFirst());
        return pageForm;
    }

    public Text createText(Long id, TextCreateForm textCreateForm, List<Hashtag> hashtagList) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if(customerOptional.isEmpty()){
            return null;
        }
        Customer customer = customerOptional.get();
        Text text = new Text().builder()
                .title(textCreateForm.getTitle())
                .content(textCreateForm.getContent())
                .customer(customer)
                .build();
        textRepository.save(text);

        for(Hashtag h : hashtagList){
            textHashtagRepository.save(new TextHashtag(text, h));
        }
        return text;
    }

    public Text updateText(Long id,TextUpdateForm textUpdateForm,List<Hashtag> hashtagList){
        //text 변경
        Optional<Text> textOptional = textRepository.findById(id);
        if(textOptional.isEmpty()){
            return null;
        }
        Text text = textOptional.get();
        text.updateText(textUpdateForm.getContent(), textUpdateForm.getTitle());
        textRepository.save(text);

        //hashTag 변경
        textHashtagRepository.deleteByText(text);
        for(Hashtag h : hashtagList){
            textHashtagRepository.save(new TextHashtag(text, h));
        }
        return text;
    }

    public Text findText(Long id){
        return textRepository.findById(id).orElse(null);
    }

    public List<Text> findText(String title){
        return textRepository.findByTitle(title);
    }

    public void deleteText(Long id){
        textRepository.deleteById(id);
    }
}
