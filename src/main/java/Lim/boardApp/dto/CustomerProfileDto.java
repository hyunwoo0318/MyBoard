package Lim.boardApp.dto;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Text;

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
