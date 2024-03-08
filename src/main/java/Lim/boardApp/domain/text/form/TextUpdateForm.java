package Lim.boardApp.domain.text.form;

import Lim.boardApp.domain.text.entity.Text;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextUpdateForm {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "내용을 입력해주세요")
    private String content;
    private String hashtags;


    public TextUpdateForm(Text text){
        this.title = text.getTitle();
        this.content = text.getContent();
    }
}
