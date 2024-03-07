package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Text;
import Lim.boardApp.dto.TextListQueryDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TextRepositoryCustom {

    public Optional<Text> queryTextWithCommentList(Long textId);

    public Optional<Text> queryTextWithHashtagList(Long textId);

    public Long updateViewCount(Long textId, Long viewCnt);

    public Page<TextListQueryDto> queryTextListWithSearch(
            String searchType,
            String textType,
            String searchKey,
            String boardName,
            Pageable pageable);
}
