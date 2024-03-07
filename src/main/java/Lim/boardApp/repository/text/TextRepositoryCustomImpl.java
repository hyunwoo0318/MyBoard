package Lim.boardApp.repository.text;

import static Lim.boardApp.domain.QBoard.board;
import static Lim.boardApp.domain.QCustomer.customer;
import static Lim.boardApp.domain.QText.*;

import static org.springframework.util.StringUtils.hasText;

import Lim.boardApp.constant.SearchType;
import Lim.boardApp.constant.TextType;
import Lim.boardApp.domain.*;
import Lim.boardApp.dto.TextListQueryDto;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TextRepositoryCustomImpl implements TextRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Text> queryTextWithCommentList(Long textId) {
        Text findText =
                queryFactory
                        .selectFrom(text)
                        .distinct()
                        .leftJoin(text.board, board)
                        .fetchJoin()
                        .leftJoin(text.commentList)
                        .fetchJoin()
                        .leftJoin(text.customer, customer)
                        .fetchJoin()
                        .where(text.id.eq(textId))
                        .fetchOne();
        return Optional.ofNullable(findText);
    }

    @Override
    public Optional<Text> queryTextWithHashtagList(Long textId) {
        Text findText =
            queryFactory
                .selectFrom(text)
                .distinct()
                .leftJoin(text.board, board)
                .fetchJoin()
                .leftJoin(text.textHashtagList)
                .fetchJoin()
                .leftJoin(text.customer, customer)
                .fetchJoin()
                .where(text.id.eq(textId))
                .fetchOne();
        return Optional.ofNullable(findText);
    }

    @Override
    public Long updateViewCount(Long textId, Long viewCnt) {
        return queryFactory
                .update(text)
                .set(text.viewCount, text.viewCount.add(viewCnt))
                .where(text.id.eq(textId))
                .execute();
    }

    @Override
    public Page<TextListQueryDto> queryTextListWithSearch(
            String searchType,
            String textType,
            String searchKey,
            String boardName,
            Pageable pageable) {
        List<Text> textList =
                queryFactory
                        .selectFrom(text)
                        .leftJoin(text.board, board)
                        .fetchJoin()
                        .where(boardNameCheck(boardName))
                        .where(searchTypeCheck(searchType, searchKey))
                        .where(textTypeCheck(textType))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        List<TextListQueryDto> textListQueryDtoList = textList.stream()
            .map(TextListQueryDto::new)
            .collect(Collectors.toList());

        Long count =
                queryFactory
                        .select(text.count())
                        .from(text)
                        .where(boardNameCheck(boardName))
                        .where(searchTypeCheck(searchType, searchKey))
                        .where(textTypeCheck(textType))
                        .fetchOne();

        return new PageImpl<>(textListQueryDtoList, pageable, count);
    }

    private BooleanExpression textTypeCheck(String textType) {
        return hasText(textType) ? text.textType.eq(TextType.valueOf(textType)) : null;
    }

    private BooleanExpression searchTypeCheck(String type, String searchKey) {
        if (!hasText(type) || !hasText(searchKey)) return null;
        if (type.equals(SearchType.ALL.name())) {
            return contentEq(searchKey).or(titleEq(searchKey));
        } else if (type.equals(SearchType.CONTENT.name())) {
            return contentEq(searchKey);
        } else if (type.equals(SearchType.TITLE.name())) {
            return titleEq(searchKey);
        }
        return null;
    }

    private BooleanExpression boardNameCheck(String boardName) {
        return boardName.equals("전체") ? null : text.board.name.eq(boardName);
    }

    private BooleanExpression contentEq(String searchKey) {
        return hasText(searchKey) ? text.content.contains(searchKey) : null;
    }

    private BooleanExpression titleEq(String searchKey) {
        return hasText(searchKey) ? text.title.contains(searchKey) : null;
    }
}
