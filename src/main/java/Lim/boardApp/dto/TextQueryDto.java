package Lim.boardApp.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextQueryDto {
    private Long id;
    private String title;
    private String content;
    private Boolean textOwn;
    private String customerName;
    private LocalDateTime createdTime;
    private List<HashtagQueryDto> hashtagList;
    private List<CommentQueryDto> commentList;
    private Boolean isBookmarked;
    private Long viewCnt;
}
