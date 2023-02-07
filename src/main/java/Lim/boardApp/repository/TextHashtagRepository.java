package Lim.boardApp.repository;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextHashtagRepository extends JpaRepository<TextHashtag, Long> {

    @Query("select th.hashtag from TextHashtag th where th.text = :text")
    public List<Hashtag> findHashtagsByText(@Param("text") Text text);

    @Query("select th.text from TextHashtag th where th.hashtag = :hashtag")
    public List<Text> findTextsByHashtag(@Param("hashtag") Hashtag hashtag);

    public void deleteAllByText(Text text);
}
