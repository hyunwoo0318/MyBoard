package Lim.boardApp.domain.comment.repository;


import static Lim.boardApp.domain.comment.entity.QComment.comment;

import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.text.entity.Text;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    @Override
    public List<Comment> findParentComments(Text text) {
        return queryFactory.selectFrom(comment)
                .where(comment.parent.isNull())
                .where(comment.text.eq(text))
                .fetch();
    }

    @Override
    public int findCommentCnt(Long textId) {
        return queryFactory.selectFrom(comment)
                .where(comment.text.id.eq(textId))
                .fetch().size();

    }

    @Override
    public List<Comment> findCommentsByCustomer(String loginId) {
        return queryFactory.selectFrom(comment)
                .where(comment.customer.loginId.eq(loginId))
                .fetch();
    }

    @Override
    public List<Comment> queryCommentByText(Long textId) {
        return queryFactory.selectFrom(comment)
                .where(comment.text.id.eq(textId))
                .fetch();
    }
}
