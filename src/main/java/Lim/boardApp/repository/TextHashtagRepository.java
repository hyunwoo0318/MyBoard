package Lim.boardApp.repository;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface TextHashtagRepository extends JpaRepository<TextHashtag, Long> {

    public List<TextHashtag> findAllByText(Text text);

    @Query("select th.hashtag from TextHashtag th where th.text = :text")
    public List<Hashtag> findHashtagsByText(@Param("text") Text text);

    @Query("select th.text from TextHashtag th where th.hashtag = :hashtag")
    public List<Text> findTextsByHashtag(@Param("hashtag") Hashtag hashtag);

    @Query("select th.text from TextHashtag th where th.hashtag = :hashtag")
    public Page<Text> findTextsByHashtag(@Param("hashtag") Hashtag hashtag, Pageable pageable);

    public void deleteByText(Text text);
}
