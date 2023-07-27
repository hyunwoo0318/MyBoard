package Lim.boardApp.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class TextCreateForm {
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    private String hashtags;

    private String boardName;

    public TextCreateForm(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public TextCreateForm(String title, String content, String hashtags, String boardName) {
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
        this.boardName = boardName;
    }
}
