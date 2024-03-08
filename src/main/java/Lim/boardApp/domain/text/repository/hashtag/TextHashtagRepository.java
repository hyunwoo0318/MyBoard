package Lim.boardApp.domain.text.repository.hashtag;

import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.entity.TextHashtag;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TextHashtagRepository extends JpaRepository<TextHashtag, Long>, TextHashtagRepositoryCustom {

    public void deleteByText(Text text);
}
