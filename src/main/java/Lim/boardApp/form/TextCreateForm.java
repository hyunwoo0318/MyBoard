package Lim.boardApp.form;

import Lim.boardApp.domain.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextCreateForm {
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    private String hashtags;

    public TextCreateForm(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
