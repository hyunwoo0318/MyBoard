package Lim.boardApp.form;

import Lim.boardApp.domain.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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
