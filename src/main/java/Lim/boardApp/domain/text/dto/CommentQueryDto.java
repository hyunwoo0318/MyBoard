package Lim.boardApp.domain.text.dto;

import Lim.boardApp.domain.comment.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class CommentQueryDto {
    private Long id;
    private String customerName;
    private LocalDateTime createdTime;
    private String content;
    private List<CommentQueryDto> childCommentList;

    public CommentQueryDto(Comment comment){
        this.id = comment.getId();
        this.customerName = comment.getCustomer().getName();
        this.createdTime = comment.getCreatedTime();
        this.content = comment.getContent();
        this.childCommentList = comment.getChildCommentList().stream().map(CommentQueryDto::new).collect(
            Collectors.toList());
    }
}
