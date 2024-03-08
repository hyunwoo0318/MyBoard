package Lim.boardApp.domain.customer.dto;

import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.text.entity.Text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileDto {
    private String loginId;
    private String email;
    private String name;
    private Integer age;

    private List<Text> textList;
    private List<Comment> commentList;
    private List<Text> bookmarkedTextList;

    
}
