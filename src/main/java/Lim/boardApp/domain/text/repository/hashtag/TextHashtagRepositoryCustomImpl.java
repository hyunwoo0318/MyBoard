package Lim.boardApp.domain.text.repository.hashtag;

import static Lim.boardApp.domain.text.entity.QText.text;
import static Lim.boardApp.domain.text.entity.QTextHashtag.textHashtag;

import Lim.boardApp.domain.text.entity.Hashtag;
import Lim.boardApp.domain.text.entity.Text;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TextHashtagRepositoryCustomImpl implements TextHashtagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteTextHashtags(Text text, List<Hashtag> hashtagList) {
        queryFactory
                .delete(textHashtag)
                .where(textHashtag.text.eq(text))
                .where(textHashtag.hashtag.in(hashtagList))
                .execute();
    }
}
