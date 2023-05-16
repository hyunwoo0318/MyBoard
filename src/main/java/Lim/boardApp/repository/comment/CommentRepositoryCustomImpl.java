package Lim.boardApp.repository.comment;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Text;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static Lim.boardApp.domain.QComment.comment;

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
    public List<Comment> findCommentsByCustomer(String loginId) {
        return queryFactory.selectFrom(comment)
                .where(comment.customer.loginId.eq(loginId))
                .fetch();
    }
}
