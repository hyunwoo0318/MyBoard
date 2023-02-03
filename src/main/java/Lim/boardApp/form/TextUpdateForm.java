package Lim.boardApp.form;

import Lim.boardApp.domain.Text;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TextUpdateForm {
    private String title;
    private String content;

    public TextUpdateForm(Text text){
        this.content = text.getContent();
        this.title = text.getTitle();
    }
}
