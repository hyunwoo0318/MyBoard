package Lim.boardApp.form;

import Lim.boardApp.domain.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TextCreateForm {
    private String title;
    private String content;
    private String hashtags;

    public TextCreateForm(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
