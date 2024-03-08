package Lim.boardApp.domain.text.service;

import static Lim.boardApp.common.exception.ExceptionInfo.NOT_FOUND;

import Lim.boardApp.common.constant.PageConst;
import Lim.boardApp.common.constant.TextType;
import Lim.boardApp.common.exception.CustomException;
import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.common.exception.UnauthorizedAccessException;
import Lim.boardApp.domain.bookmark.repository.BookmarkRepository;
import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.comment.form.CommentCreateForm;
import Lim.boardApp.domain.comment.repository.CommentRepository;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.dto.CommentQueryDto;
import Lim.boardApp.domain.text.dto.HashtagQueryDto;
import Lim.boardApp.domain.text.dto.TextListQueryDto;
import Lim.boardApp.domain.text.dto.TextQueryDto;
import Lim.boardApp.domain.text.entity.Board;
import Lim.boardApp.domain.text.entity.Hashtag;
import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.entity.TextHashtag;
import Lim.boardApp.domain.text.form.TextCreateForm;
import Lim.boardApp.domain.text.form.TextUpdateForm;
import Lim.boardApp.domain.text.repository.board.BoardRepository;
import Lim.boardApp.domain.text.repository.hashtag.HashtagRepository;
import Lim.boardApp.domain.text.repository.hashtag.TextHashtagRepository;
import Lim.boardApp.domain.text.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;
    private final BoardRepository boardRepository;
    private final TextHashtagRepository textHashtagRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;

    private final ViewCountService viewCountService;



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

    public void addComment(Customer customer, CommentCreateForm commentCreateForm, Long textId)
            throws NotFoundException {

        // text검증
        Text text = checkText(textId);

        Comment comment = null;
        Long parentCommentId = commentCreateForm.getParent();
        if (parentCommentId != null) {
            Comment parentComment = new Comment(parentCommentId);
            comment = new Comment(text, customer, commentCreateForm.getContent(), parentComment);
            comment.setChildCommentList(parentComment);
        } else {
            comment = new Comment(text, customer, commentCreateForm.getContent());
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
            viewCountService.increaseViewCnt(text, customer.getId());
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
