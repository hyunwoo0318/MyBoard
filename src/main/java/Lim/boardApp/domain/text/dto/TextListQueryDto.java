package Lim.boardApp.domain.text.dto;

import Lim.boardApp.domain.text.entity.Text;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TextListQueryDto {

    private Long id;
    private String title;
    private String boardName;
    private LocalDateTime createdTime;
    private String customerName;
    private String textType;
    private Long viewCount;

    public TextListQueryDto(Text text) {
        this.id = text.getId();
        this.title = text.getTitle();
        this.boardName = text.getBoard().getName();
        this.createdTime = text.getCreatedTime();
        this.customerName = text.getCustomer().getName();
        this.textType = text.getTextType().name();
        this.viewCount = text.getViewCount();
    }
}
