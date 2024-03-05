package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.ObjectValue.TextType;
import Lim.boardApp.domain.*;
import Lim.boardApp.dto.CommentQueryDto;
import Lim.boardApp.dto.HashtagQueryDto;
import Lim.boardApp.dto.TextQueryDto;
import Lim.boardApp.form.*;
import Lim.boardApp.repository.*;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.comment.CommentRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;
    private final BoardRepository boardRepository;
    private final TextHashtagRepository textHashtagRepository;
    private final RedisTemplate redisTemplate;
    private final CustomerRepository customerRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;


    public PageForm pagingByAll(int page,int pageSize,int blockSize,String boardName, String textType){
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        List<Text> resultList = new ArrayList<>();
        if(boardName.equals("전체")){
            if (textType == null) {
                resultList = textRepository.findAll();
            } else if (textType.equals(TextType.GENERAL.name())) {
                resultList = textRepository.queryGeneralTexts();
            } else if (textType.equals(TextType.ARTICLE.name())) {
                resultList = textRepository.queryArticleTexts();
            } else return null;
        }else{
            Board board = boardRepository.findByName(boardName).orElseThrow(() -> {
                throw new NotFoundException();
            });
            if (textType == null) {
                resultList = textRepository.queryTextByBoard(boardName);
            } else if (textType.equals(TextType.GENERAL.name())) {
                resultList = textRepository.queryGeneralTexts(boardName);
            } else if (textType.equals(TextType.ARTICLE.name())
            ) {
                resultList = textRepository.queryArticleTexts(boardName);
            } else return null;
        }
        Page<Text> findPage = makePage(resultList, pageRequest, blockSize);
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

        Page<Text> findPage = makePage(resultList, pageRequest, blockSize);
        return makePageForm(findPage, page, blockSize);
    }

    private Page<Text> makePage(List<Text> textList, PageRequest pageRequest, int blockSize) {
        int first = Math.min(Long.valueOf(pageRequest.getOffset()).intValue(), textList.size());
        int last = Math.min(first + pageRequest.getPageSize(), textList.size());

        return new PageImpl<Text>(textList.subList(first, last), pageRequest, blockSize);
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

    public Text createText(Customer customer, TextCreateForm textCreateForm) {

        Board board = boardRepository.findByName(textCreateForm.getBoardName()).orElseThrow(() -> {
            throw new NotFoundException();
        });

        //글 저장
        Text text = Text.builder()
                .title(textCreateForm.getTitle())
                .content(textCreateForm.getContent())
                .customer(customer)
                .textType(TextType.GENERAL)
                .board(board)
                .build();
        textRepository.save(text);

        //해시태그 저장
        List<Hashtag> hashtagList = parseHashtag(textCreateForm.getHashtags());
        List<TextHashtag> textHashtagList = hashtagList.stream().map(h -> new TextHashtag(text, h)).collect(Collectors.toList());
        textHashtagRepository.saveAll(textHashtagList);
        return text;
    }

    public Boolean isOwner(Long textId, Long customerId) {
        Customer customer = checkCustomer(customerId);

        Text text = checkText(textId);
        Long ownerId = text.getCustomer().getId();
        return ownerId == customerId;
    }

    private Customer checkCustomer(Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> {
            throw new NotFoundException();
        });
    }

    private Text checkText (Long textId){
        return textRepository.findById(textId).orElseThrow(() -> {
            throw new NotFoundException();
        });
    }


    public TextUpdateForm setTextUpdateForm(Long textId) {
        Text text = checkText(textId);

        List<Hashtag> hashtagList = textHashtagRepository.findHashtagsByText(text);
        String hashtags = mergeHashtag(hashtagList);

        return new TextUpdateForm(text.getTitle(), text.getContent(), hashtags);
    }

    @Transactional
    public Text updateText(Long textId,TextUpdateForm textUpdateForm){
        //text 검증
        Text text = checkText(textId);

        //텍스트 변경
        text.updateText(textUpdateForm.getContent(), textUpdateForm.getTitle());

        //hashTag 변경
        List<Hashtag> existedHashtagList = textHashtagRepository.findHashtagsByText(text);
        List<Hashtag> newHashgtagList = parseHashtag(textUpdateForm.getHashtags());
        updateHashtags(textId, existedHashtagList, newHashgtagList);
        return text;
    }

    @Transactional
    public void updateHashtags(Long textId, List<Hashtag> existedHashtagList, List<Hashtag> newHashgtagList) {
        //text 검증
        Text text = checkText(textId);

        //새로 저장할 해시태그 찾기 -> 기존에 존재하지 않는 해시태그들
        List<Hashtag> addHashtagList = newHashgtagList.stream()
                .filter(h -> !existedHashtagList.contains(h))
                .collect(Collectors.toList());

        //삭제할 해시태그 찾기 -> 기존제 존재했지만 없어진 해시태그들
        List<Hashtag> deleteHashtagList = existedHashtagList.stream()
                .filter(h -> !newHashgtagList.contains(h))
                .collect(Collectors.toList());

        //해시태그 해당 텍스트에 추가
        List<TextHashtag> newTextHashtagList = addHashtagList.stream()
                .map(h -> new TextHashtag(text, h)).collect(Collectors.toList());
        textHashtagRepository.saveAll(newTextHashtagList);

        //해시태그 해당 텍스트에서 삭제
        textHashtagRepository.deleteTextHashtags(text, deleteHashtagList);
    }


    public List<Text> findTextByCustomer(String loginId) {
        return textRepository.queryTextByCustomer(loginId);
    }

    @Transactional
    public void deleteText(Long textId){
        //text 검증
        Text text = textRepository.findById(textId).orElseThrow(() -> {
            throw new NotFoundException();
        });

        //해당 글에 존재하는 hashtag리스트 삭제
        textHashtagRepository.deleteByText(text);

        //글 삭제
        textRepository.deleteById(textId);
    }

    public List<Comment> findParentCommentList(Text text) {
        return commentRepository.findParentComments(text);
    }

    public int findCommentCnt(Long textId) {
        return commentRepository.findCommentCnt(textId);
    }

    public void addComment(Customer customer, CommentForm commentForm, Long textId) throws NotFoundException {

        //text검증
        Text text = checkText(textId);

        Comment comment = null;
        if (commentForm.getParent() != null) {
            Comment parent = commentRepository.findById(commentForm.getParent()).orElseThrow(() -> {
                throw new NotFoundException();
            });

            comment = new Comment(text, customer, commentForm.getContent(), parent);
            comment.setChildCommentList(parent);
        }else{
            comment = new Comment(text, customer, commentForm.getContent());
        }
        commentRepository.save(comment);
    }

    public List<Comment> findCommentsByCustomer(String loginId) {
        return commentRepository.findCommentsByCustomer(loginId);
    }

    /**
     * ,를 구분자로 받은 hashtag를 파싱해서 새로운 해시태그면 저장하는 방식
     * @param ','를 구분자로 가지는 hashtag모음
     * @return List<Hashtag>
     */
    public List<Hashtag> parseHashtag(String hashtags){
        String[] tagList = hashtags.replaceAll(" ", "").split(",");
        List<Hashtag> hashtagList = new ArrayList<>();
        for (String t : tagList) {
            Optional<Hashtag> hashtagOptional = hashtagRepository.findByName(t);
            if(hashtagOptional.isEmpty()){
                //새로운 hashtag
                Hashtag newTag = new Hashtag(t);
                hashtagRepository.save(newTag);
                hashtagList.add(newTag);
            }else {
                Hashtag hashtag = hashtagOptional.get();
                hashtagList.add(hashtag);
            }
        }
        return hashtagList;
    }

    public List<Hashtag> findHashtagList(Text text) {
        return textHashtagRepository.findHashtagsByText(text);
    }

    /**
     * hashtagList를 다시 사용자가 수정할수 있게 원래의 형식으로 돌려놓음
     * @param hashtagList
     * @return String(','로 구분된 hashtag집합)
     */
    public String mergeHashtag(List<Hashtag> hashtagList) {
        return hashtagList.stream().map(Hashtag::getName).collect(Collectors.joining(","));
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

    @Transactional
    public TextQueryDto showText(Customer customer, Long textId) {

        boolean isBookmarked = false;
        boolean textOwn = true;

        Text text = textRepository.queryText(textId).orElseThrow( () -> {
            throw new NotFoundException();
        });

        if(bookmarkRepository.queryBookmark(text, customer).isPresent()){
            isBookmarked = true;
        }

        if(!text.getCustomer().equals(customer)){
           textOwn = false;
           increaseViewCnt(text, customer.getId());
        }

        List<CommentQueryDto> commentList = text.getCommentList().stream()
            .map(CommentQueryDto::new)
            .collect(Collectors.toList());

        List<HashtagQueryDto> hashTagList = textHashtagRepository.findHashtagsByText(text).stream()
            .map(HashtagQueryDto::new)
            .collect(Collectors.toList());

        return new TextQueryDto(text.getId(),text.getTitle(), text.getContent(),
            textOwn,customer.getName(), text.getCreatedTime(), hashTagList, commentList, isBookmarked,text.getViewCount());


    }
}


