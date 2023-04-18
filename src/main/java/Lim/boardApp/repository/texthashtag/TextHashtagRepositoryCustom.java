package Lim.boardApp.repository.texthashtag;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextHashtagRepositoryCustom {

    public List<Hashtag> findHashtagsByText(Text text);


    public List<Text> findTextsByHashtag(Hashtag hashtag);


    public Page<Text> findTextsByHashtag(Hashtag hashtag, Pageable pageable);
}
