package Lim.boardApp.repository.texthashtag;

import Lim.boardApp.domain.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static Lim.boardApp.domain.QHashtag.hashtag;
import static Lim.boardApp.domain.QText.text;
import static Lim.boardApp.domain.QTextHashtag.textHashtag;

@Repository
@RequiredArgsConstructor
public class TextHashtagRepositoryCustomImpl implements TextHashtagRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    @Override
    public List<Hashtag> findHashtagsByText(Text text) {
        return queryFactory.select(hashtag)
                .from(textHashtag)
                .where(textHashtag.text.eq(text))
                .fetch();
    }

    @Override
    public List<Text> findTextsByHashtag(Hashtag hashtag) {
        return queryFactory.select(text)
                .from(textHashtag)
                .where(textHashtag.hashtag.eq(hashtag))
                .fetch();
    }

    @Override
    public Page<Text> findTextsByHashtag(Hashtag hashtag, Pageable pageable) {
        List<Text> textList = queryFactory.select(text)
                .from(textHashtag)
                .where(textHashtag.hashtag.eq(hashtag))
                .fetch();
        return new PageImpl<>(textList, pageable, textList.size());
    }
}
