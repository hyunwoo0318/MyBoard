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
    public List<Text> findTextsByHashtag(String searchKey) {
        return queryFactory.select(text)
                .from(textHashtag)
                .where(textHashtag.hashtag.name.eq(searchKey))
                .fetch();
    }

    @Override
    public Page<Text> findTextsByHashtag(String searchKey, Pageable pageable) {
        List<Text> textList = queryFactory.select(text)
                .from(textHashtag)
                .where(textHashtag.hashtag.name.eq(searchKey))
                .fetch();
        return new PageImpl<>(textList, pageable, textList.size());
    }

    @Override
    public void deleteTextHashtags(Text text, List<Hashtag> hashtagList) {
        queryFactory.delete(textHashtag)
                .where(textHashtag.text.eq(text))
                .where(textHashtag.hashtag.in(hashtagList))
                .execute();
    }
}
