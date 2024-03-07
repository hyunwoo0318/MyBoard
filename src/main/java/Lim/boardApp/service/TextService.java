package Lim.boardApp.service;

import static Lim.boardApp.exception.ExceptionInfo.NOT_FOUND;

import Lim.boardApp.constant.PageConst;
import Lim.boardApp.constant.TextType;
import Lim.boardApp.domain.*;
import Lim.boardApp.dto.CommentQueryDto;
import Lim.boardApp.dto.HashtagQueryDto;
import Lim.boardApp.dto.TextListQueryDto;
import Lim.boardApp.dto.TextQueryDto;
import Lim.boardApp.exception.CustomException;
import Lim.boardApp.exception.NotFoundException;
import Lim.boardApp.exception.UnauthorizedAccessException;
import Lim.boardApp.form.*;
import Lim.boardApp.repository.*;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.comment.CommentRepository;
import Lim.boardApp.repository.text.TextRepository;
import Lim.boardApp.repository.texthashtag.TextHashtagRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
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
    private final BookmarkRepository bookmarkRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;



    @Transactional(readOnly = true)
    public Page<TextListQueryDto> queryTextListWithSearchAndSort(
            int page, String searchKey, String searchType, String textType, String boardName,
        String sort) {
        PageRequest pageRequest = PageRequest.of(page, PageConst.PAGE_SIZE);
        return textRepository.queryTextListWithSearch(
                searchType, textType, searchKey, boardName,sort, pageRequest);
    }


    @Transactional
    public Text createText(Customer customer, TextCreateForm textCreateForm) {

        Board board =
                boardRepository
                        .findByName(textCreateForm.getBoardName())
                        .orElseThrow(() -> new CustomException(NOT_FOUND));

        // 글 저장
        Text text =
                Text.builder()
                        .title(textCreateForm.getTitle())
                        .content(textCreateForm.getContent())
                        .customer(customer)
                        .textType(TextType.GENERAL)
                        .board(board)
                        .build();
        textRepository.save(text);

        // 해시태그 저장
        List<Hashtag> hashtagList = parseHashtag(textCreateForm.getHashtags());
        List<TextHashtag> textHashtagList =
                hashtagList.stream()
                        .map(h -> new TextHashtag(text, h))
                        .collect(Collectors.toList());
        textHashtagRepository.saveAll(textHashtagList);
        return text;
    }

    private void isOwner(Customer textCustomer, Customer loginCustomer) {
        if(!textCustomer.getId().equals(loginCustomer.getId())){
            throw new UnauthorizedAccessException();
        }
    }

    private Text checkText(Long textId) {
        return textRepository
                .findById(textId)
                .orElseThrow(
                        () -> {
                            throw new CustomException(NOT_FOUND);
                        });
    }

    @Transactional(readOnly = true)
    public TextUpdateForm setTextUpdateForm(Long textId, Customer loginCustomer) {
        Text text = textRepository.queryTextWithHashtagList(textId)
                                    .orElseThrow(() -> new CustomException(NOT_FOUND));

        //글 주인 체크
        isOwner(text.getCustomer(), loginCustomer);

        List<Hashtag> hashtagList = text.getTextHashtagList().stream()
            .map(TextHashtag::getHashtag)
            .collect(Collectors.toList());
        String hashtags = mergeHashtag(hashtagList);

        return new TextUpdateForm(text.getTitle(), text.getContent(), hashtags);
    }

    @Transactional
    public Text updateText(Long textId, TextUpdateForm textUpdateForm) {
        // text 검증
        Text text =
                textRepository
                        .queryTextWithHashtagList(textId)
                        .orElseThrow(() -> new CustomException(NOT_FOUND));

        // 텍스트 변경
        text.updateText(textUpdateForm.getContent(), textUpdateForm.getTitle());

        // hashTag 변경
        List<Hashtag> existedHashtagList = text.getTextHashtagList().stream()
            .map(TextHashtag::getHashtag)
            .collect(Collectors.toList());
        List<Hashtag> newHashgtagList = parseHashtag(textUpdateForm.getHashtags());
        updateHashtags(text, existedHashtagList, newHashgtagList);
        return text;
    }

    @Transactional
    public void updateHashtags(
            Text text, List<Hashtag> existedHashtagList, List<Hashtag> newHashgtagList) {

        // 새로 저장할 해시태그 찾아서 저장 -> 기존에 존재하지 않는 해시태그들
        List<TextHashtag> newHashtagList = newHashgtagList.stream()
            .filter(h -> !existedHashtagList.contains(h))
            .map(h -> new TextHashtag(text, h))
            .collect(Collectors.toList());

        textHashtagRepository.saveAll(newHashtagList);

        // 삭제할 해시태그 찾기 -> 기존에 존재했지만 없어진 해시태그들
        List<Hashtag> deleteHashtagList =
                existedHashtagList.stream()
                        .filter(h -> !newHashgtagList.contains(h))
                        .collect(Collectors.toList());

        textHashtagRepository.deleteTextHashtags(text, deleteHashtagList);
   }

    @Transactional
    public void deleteText(Long textId, Customer customer) {

        Text text = textRepository.queryTextWithCommentList(textId)
            .orElseThrow(() -> new CustomException(NOT_FOUND));

        isOwner(text.getCustomer(), customer);

        // 해당 글에 존재하는 hashtag리스트 삭제
        textHashtagRepository.deleteByText(text);

        // 글 삭제
        textRepository.deleteById(textId);
    }

    public void addComment(Customer customer, CommentForm commentForm, Long textId)
            throws NotFoundException {

        // text검증
        Text text = checkText(textId);

        Comment comment = null;
        Long parentCommentId = commentForm.getParent();
        if (parentCommentId != null) {
            Comment parentComment = new Comment(parentCommentId);
            comment = new Comment(text, customer, commentForm.getContent(), parentComment);
            comment.setChildCommentList(parentComment);
        } else {
            comment = new Comment(text, customer, commentForm.getContent());
        }
        commentRepository.save(comment);
    }

    /**
     * ,를 구분자로 받은 hashtag를 파싱해서 새로운 해시태그면 저장하는 방식
     *
     * @param ','를 구분자로 가지는 hashtag모음
     * @return List<Hashtag>
     */
    private List<Hashtag> parseHashtag(String hashtags) {
        List<String> tagNameList = Arrays.asList(hashtags.replaceAll(" ", "").split(","));
        Map<String, Hashtag> prevHashtagMap = hashtagRepository.findByNameIn(tagNameList).stream()
            .collect(Collectors.toMap(Hashtag::getName, h -> h));

        List<Hashtag> newHashtagList = tagNameList.stream()
            .filter(name -> prevHashtagMap.get(name) == null)
            .map(Hashtag::new)
            .collect(Collectors.toList());

        hashtagRepository.saveAll(newHashtagList);

        return newHashtagList;
    }

    /**
     * hashtagList를 다시 사용자가 수정할수 있게 원래의 형식으로 돌려놓음
     *
     * @param hashtagList
     * @return String(','로 구분된 hashtag집합)
     */
    private String mergeHashtag(List<Hashtag> hashtagList) {
        return hashtagList.stream().map(Hashtag::getName).collect(Collectors.joining(","));
    }

    /**
     * 조회수를 증가시키는 메서드
     *
     * @param text 조회하는 글
     * @param customerId 조회하는 회원의 ID
     * @return 조회수
     *     <p>조회수 증가 조건 1. 작성자가 아닌 다른 사람이 조회할때만 증가함 2. 해당 글을 조회한 사람은 6시간 이내에는 해당 글을 조회해도 조회수가
     *     증가하지않음.
     *     <p>구현 : redis를 이용한 캐싱을 통해 조회수를 캐시에 저장해 두었다가 3분에 한번씩 DB에 업데이트
     *     <p>조회수 관련 redis key naming - 캐싱한 조회수(즉 DB update시 올려야하는 조회수): viewCnt::{textId} - 해당 글을
     *     조회했는지 체크 : viewCheck::{textId}::{customerId}
     *     <p>해당 키의 유효기간을 6시간으로 설정하고 해당 키로 조회했을때 redis상에 존재하면 조회수를 올리지않음. 해당 키로 조회했을때 redis상에 존재하지
     *     않을경우, 해당키를 redis에 저장하고 조회수를 1 올림.
     */
    public Long increaseViewCnt(Text text, Long customerId) {
        String keyForCnt = "viewCnt::" + text.getId();
        String keyForCheck = "viewCheck::" + text.getId() + "::" + customerId;

        ValueOperations ops = redisTemplate.opsForValue();
        if (ops.get(keyForCheck) == null) {
            // 해당 키가 존재하지 않을 경우 -> 조회수를 1상승
            ops.set(keyForCheck, "T", Duration.ofHours(6));
            if (ops.get(keyForCnt) == null) {
                ops.set(keyForCnt, "1");
            } else {
                ops.increment(keyForCnt);
            }
        }

        return text.getViewCount();
    }

    /** 3분에 한번씩 캐시에 있는 조회수를 바탕으로 조회수 상승 */
    @Scheduled(cron = "0 0/3 * * * ?")
    @Transactional
    public void updateViewCount() {
        Set<String> keySet = redisTemplate.keys("viewCnt*");
        Iterator<String> itr = keySet.iterator();

        while (itr.hasNext()) {
            String viewCntKey = itr.next();

            Long textId = Long.parseLong(viewCntKey.split("::")[1]);
            Long viewCnt = Long.parseLong((String) redisTemplate.opsForValue().get(viewCntKey));

            Long updatedViewCnt = textRepository.updateViewCount(textId, viewCnt);

            redisTemplate.delete(viewCntKey);
        }
    }

    @Transactional
    public TextQueryDto showText(Customer customer, Long textId) {

        boolean isBookmarked = false;
        boolean textOwn = true;

        Text text =
                textRepository
                        .queryTextWithCommentList(textId)
                        .orElseThrow(
                                () -> {
                                    throw new NotFoundException();
                                });

        //북마크 여부 체크
        if (bookmarkRepository.queryBookmark(text, customer).isPresent()) {
            isBookmarked = true;
        }

        //글의 주인인지 아닌지 체크
        if (!text.getCustomer().equals(customer)) {
            textOwn = false;
            increaseViewCnt(text, customer.getId());
        }

        //댓글, 해시태그 목록 조회
        List<CommentQueryDto> commentList =
                text.getCommentList().stream()
                        .map(CommentQueryDto::new)
                        .collect(Collectors.toList());

        List<HashtagQueryDto> hashTagList =
                text.getTextHashtagList().stream()
                        .map(textHashtag -> {
                            return new HashtagQueryDto(textHashtag.getHashtag());
                        })
                        .collect(Collectors.toList());

        return new TextQueryDto(
                text.getId(),
                text.getTitle(),
                text.getContent(),
                textOwn,
                customer.getName(),
                text.getCreatedTime(),
                hashTagList,
                commentList,
                isBookmarked,
                text.getViewCount());
    }
}
