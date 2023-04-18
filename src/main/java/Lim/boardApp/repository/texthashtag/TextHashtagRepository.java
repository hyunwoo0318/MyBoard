package Lim.boardApp.repository.texthashtag;

import Lim.boardApp.domain.Text;
import Lim.boardApp.domain.TextHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextHashtagRepository extends JpaRepository<TextHashtag, Long>, TextHashtagRepositoryCustom {

    public void deleteByText(Text text);
}
