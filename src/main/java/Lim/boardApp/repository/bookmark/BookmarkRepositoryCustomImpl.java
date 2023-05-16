package Lim.boardApp.repository.bookmark;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static Lim.boardApp.domain.QBookmark.bookmark;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteBookmark(Text text, Customer customer) {
        queryFactory.delete(bookmark)
                .where(bookmark.text.eq(text).and(bookmark.customer.eq(customer)))
                .execute();
    }

    @Override
    public List<Text> findBookmarkedTextsByCustomer(String loginId) {
        return queryFactory.select(bookmark.text)
                .from(bookmark)
                .where(bookmark.customer.loginId.eq(loginId))
                .fetch();
    }

}
