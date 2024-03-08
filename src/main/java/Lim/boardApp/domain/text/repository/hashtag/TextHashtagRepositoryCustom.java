package Lim.boardApp.domain.text.repository.hashtag;

import Lim.boardApp.domain.text.entity.Hashtag;
import Lim.boardApp.domain.text.entity.Text;

import java.util.List;

public interface TextHashtagRepositoryCustom {

    public void deleteTextHashtags(Text text, List<Hashtag> hashtagList);
}
