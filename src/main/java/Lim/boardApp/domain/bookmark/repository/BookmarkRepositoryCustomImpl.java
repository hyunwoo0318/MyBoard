package Lim.boardApp.domain.bookmark.repository;

import static Lim.boardApp.domain.bookmark.entity.QBookmark.bookmark;

import Lim.boardApp.domain.bookmark.entity.Bookmark;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.entity.Text;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteBookmark(Text text, Customer customer) {
        queryFactory
                .delete(bookmark)
                .where(bookmark.text.eq(text).and(bookmark.customer.eq(customer)))
                .execute();
    }

    @Override
    public Optional<Bookmark> queryBookmark(Text text, Customer customer) {
        Bookmark findBookmark =
                queryFactory
                        .selectFrom(bookmark)
                        .where(bookmark.customer.eq(customer))
                        .where(bookmark.text.eq(text))
                        .fetchFirst();

        return Optional.ofNullable(findBookmark);
    }
}
