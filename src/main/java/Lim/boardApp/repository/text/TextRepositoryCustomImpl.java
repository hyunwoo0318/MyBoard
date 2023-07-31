package Lim.boardApp.repository.text;

import Lim.boardApp.ObjectValue.TextType;
import Lim.boardApp.domain.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static Lim.boardApp.domain.QBookmark.bookmark;
import static Lim.boardApp.domain.QComment.comment;
import static Lim.boardApp.domain.QHashtag.hashtag;
import static Lim.boardApp.domain.QText.*;
import static Lim.boardApp.domain.QTextHashtag.textHashtag;

@Repository
@RequiredArgsConstructor
public class TextRepositoryCustomImpl implements TextRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Text> queryGeneralTexts() {
        return queryFactory.selectFrom(text)
                .where(text.textType.eq(TextType.GENERAL))
                .fetch();
    }

    @Override
    public List<Text> queryGeneralTexts(String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.textType.eq(TextType.GENERAL))
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> queryArticleTexts() {
        return queryFactory.selectFrom(text)
                .where(text.textType.eq(TextType.ARTICLE))
                .fetch();
    }

    @Override
    public List<Text> queryArticleTexts(String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.textType.eq(TextType.ARTICLE))
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> queryTextByBoard(String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> searchTextByContentTitle(String searchKey) {
        return queryFactory.selectFrom(text)
                .where(text.content.contains(searchKey).or(text.title.contains(searchKey)))
                .fetch();
    }

    @Override
    public List<Text> searchTextByContentTitle(String searchKey, String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.content.contains(searchKey).or(text.title.contains(searchKey)))
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> searchTextByContent(String searchKey) {
        return queryFactory.selectFrom(text)
                .where(text.content.contains(searchKey))
                .fetch();
    }

    @Override
    public List<Text> searchTextByContent(String searchKey, String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.content.contains(searchKey))
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> searchTextByTitle(String searchKey) {
        return queryFactory.selectFrom(text)
                .where(text.title.contains(searchKey))
                .fetch();
    }

    @Override
    public List<Text> searchTextByTitle(String searchKey, String boardName) {
        return queryFactory.selectFrom(text)
                .where(text.title.contains(searchKey))
                .where(text.board.name.eq(boardName))
                .fetch();
    }

    @Override
    public List<Text> queryTextByCustomer(String loginId) {
        return queryFactory.selectFrom(text)
                .where(text.customer.loginId.eq(loginId))
                .fetch();
    }

    @Override
    public Long updateViewCount(Long textId, Long viewCnt) {
        return queryFactory.update(text)
                .set(text.viewCount,text.viewCount.add(viewCnt))
                .where(text.id.eq(textId))
                .execute();
    }

}
