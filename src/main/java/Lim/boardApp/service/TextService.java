package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.*;
import Lim.boardApp.form.PageBlockForm;
import Lim.boardApp.form.PageForm;
import Lim.boardApp.form.TextCreateForm;
import Lim.boardApp.form.TextUpdateForm;
import Lim.boardApp.repository.*;
import Lim.boardApp.repository.text.TextRepository;
import Lim.boardApp.repository.texthashtag.TextHashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TextService {

    private final TextRepository textRepository;
    private final TextHashtagRepository textHashtagRepository;
    private final CustomerRepository customerRepository;
    private final HashtagRepository hashtagRepository;

    private final BoardRepository boardRepository;

    private final RedisTemplate redisTemplate;

    public PageForm pagingByAll(int page,int pageSize,int blockSize,String boardName){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<Text> findPage = null;
        if(boardName.equals("전체")){
            findPage = textRepository.findAll(pageRequest);
        }else{
            Board board = boardRepository.findByName(boardName).orElseThrow(() -> {
                throw new NotFoundException();
            });

            findPage = textRepository.findByBoard(board, pageRequest);
            //findPage = textRepository.searchTextByBoardName(boardName, pageRequest);
        }
        return makePageForm(findPage, page, blockSize);
    }

    public PageForm pagingBySearch(int page, int pageSize, int blockSize, String searchKey, String type, String boardName){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        List<Text> resultList = new ArrayList<>();
        if(boardName.equals("전체")) {
            if (type.equals("all")) {
                resultList = textRepository.searchTextByContentTitle(searchKey);
            } else if (type.equals("content")) {
                resultList = textRepository.searchTextByContent(searchKey);
            } else if (type.equals("title")) {
                resultList = textRepository.searchTextByTitle(searchKey);
            } else if (type.equals("hashtag")) {
                resultList = textHashtagRepository.findTextsByHashtag(searchKey);
            } else return null;
        }else{
            if (type.equals("all")) {
                resultList = textRepository.searchTextByContentTitle(searchKey,boardName);
            } else if (type.equals("content")) {
                resultList = textRepository.searchTextByContent(searchKey,boardName);
            } else if (type.equals("title")) {
                resultList = textRepository.searchTextByTitle(searchKey,boardName);
            } else if (type.equals("hashtag")) {
                resultList = textHashtagRepository.findTextsByHashtag(searchKey);
            } else return null;
        }

        int first = Math.min(new Long(pageRequest.getOffset()).intValue(), resultList.size());
        int last = Math.min(first + pageRequest.getPageSize(), resultList.size());

        Page<Text> findPage = new PageImpl<Text>(resultList.subList(first, last), pageRequest, blockSize);
        return makePageForm(findPage, page, blockSize);

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

        PageForm pageForm = new PageForm(start,end, end-start + 1, page, lastPage, findPage.getContent(), findPage.isLast(), findPage.isFirst());
        return pageForm;
    }

    public Text createText(Long id, TextCreateForm textCreateForm, List<Hashtag> hashtagList,String fileName) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException();
        });

        Board board = boardRepository.findByName(textCreateForm.getBoardName()).orElseThrow(() -> {
            throw new NotFoundException();
        });

        Text text = new Text().builder()
                .title(textCreateForm.getTitle())
                .content(textCreateForm.getContent())
                .customer(customer)
                .fileName(fileName)
                .board(board)
                .build();
        textRepository.save(text);

        List<TextHashtag> textHashtagList = new ArrayList<>();
        for(Hashtag h : hashtagList){
            textHashtagList.add(new TextHashtag(text, h));
        }
        textHashtagRepository.saveAll(textHashtagList);
        return text;
    }

    public Text updateText(Long id,TextUpdateForm textUpdateForm,List<Hashtag> hashtagList){
        //text 변경
        Text text = textRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException();
        });

        text.updateText(textUpdateForm.getContent(), textUpdateForm.getTitle(),null);

        //hashTag 변경
        List<TextHashtag> textHashtagList = new ArrayList<>();
        for(Hashtag h : hashtagList){
            textHashtagList.add(new TextHashtag(text, h));
        }
        textHashtagRepository.saveAll(textHashtagList);
        return text;
    }

    public Text findText(Long id){
        return textRepository.findById(id).orElseThrow(() ->{
            throw new NotFoundException();
        });
    }


    public List<Text> findTextByCustomer(String loginId) {
        return textRepository.queryTextByCustomer(loginId);
    }

    public void deleteText(Long id){
        textRepository.deleteById(id);
    }

    /**
     * 조회수를 증가시키는 메서드
     * @param text 조회하는 글
     * @param customerId 조회하는 회원의 ID
     * @return 조회수
     *
     * 조회수 증가 조건
     * 1. 작성자가 아닌 다른 사람이 조회할때만 증가함
     * 2. 해당 글을 조회한 사람은 6시간 이내에는 해당 글을 조회해도 조회수가 증가하지않음.
     *
     * 구현 : redis를 이용한 캐싱을 통해 조회수를 캐시에 저장해 두었다가 3분에 한번씩 DB에 업데이트
     *
     * 조회수 관련 redis key naming
     * -  캐싱한 조회수(즉 DB update시 올려야하는 조회수): viewCnt::{textId}
     * -  해당 글을 조회했는지 체크 : viewCheck::{textId}::{customerId}
     *
     * 해당 키의 유효기간을 6시간으로 설정하고 해당 키로 조회했을때 redis상에 존재하면 조회수를 올리지않음.
     * 해당 키로 조회했을때 redis상에 존재하지 않을경우, 해당키를 redis에 저장하고 조회수를 1 올림.
     */
    public Long increaseViewCnt(Text text, Long customerId) {
        String keyForCnt = "viewCnt::" + text.getId();
        String keyForCheck = "viewCheck::" + text.getId() + "::" + customerId;

        ValueOperations ops = redisTemplate.opsForValue();
        if (ops.get(keyForCheck) == null) {
            //해당 키가 존재하지 않을 경우 -> 조회수를 1상승
            ops.set(keyForCheck, "T", Duration.ofHours(6));
            if (ops.get(keyForCnt) == null) {
                ops.set(keyForCnt, "1");
            }else {
                ops.increment(keyForCnt);
            }
        }

        return text.getViewCount();
    }

    /**
     * 3분에 한번씩 캐시에 있는 조회수를 바탕으로 조회수 상승
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    @Transactional
    public void updateViewCount() {
        Set<String> keySet = redisTemplate.keys("viewCnt*");
        Iterator<String> itr = keySet.iterator();

        while (itr.hasNext()) {
            String viewCntKey = itr.next();

            Long textId = Long.parseLong(viewCntKey.split("::")[1]);
            Long viewCnt = Long.parseLong((String)redisTemplate.opsForValue().get(viewCntKey));

            Long updatedViewCnt = textRepository.updateViewCount(textId, viewCnt);

            redisTemplate.delete(viewCntKey);
        }
    }
}
