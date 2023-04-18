package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Text;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static Lim.boardApp.domain.QText.*;

@Repository
@RequiredArgsConstructor
public class TextRepositoryCustomImpl implements TextRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Text> searchTextByContentTitle(String searchKey, Pageable pageable) {
        List<Text> textList = queryFactory.selectFrom(text)
                .where(text.content.like(searchKey).or(text.title.like(searchKey)))
                .fetch();
        return new PageImpl<>(textList, pageable, textList.size());
    }

    @Override
    public Page<Text> searchTextByContent(String searchKey, Pageable pageable) {
        List<Text> textList = queryFactory.selectFrom(text)
                .where(text.content.like(searchKey))
                .fetch();
        return new PageImpl<>(textList, pageable, textList.size());
    }

    @Override
    public Page<Text> searchTextByTitle(String searchKey, Pageable pageable) {
        List<Text> textList = queryFactory.selectFrom(text)
                .where(text.title.like(searchKey))
                .fetch();
        return new PageImpl<>(textList, pageable, textList.size());
    }
}
