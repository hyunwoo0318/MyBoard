package Lim.boardApp.domain.text.repository.text;

import Lim.boardApp.domain.text.dto.TextListQueryDto;
import Lim.boardApp.domain.text.entity.Text;

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
            String sort,
            Pageable pageable);
}
