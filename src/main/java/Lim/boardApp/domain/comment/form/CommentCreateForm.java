package Lim.boardApp.domain.comment.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateForm {

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    private Long parent;
}
