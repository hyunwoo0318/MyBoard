package Lim.boardApp.form;

import Lim.boardApp.domain.Hashtag;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.TextHashtagRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Data
@NoArgsConstructor
public class TextUpdateForm {
    private String title;
    private String content;
    private String hashtags;

    public TextUpdateForm(Text text){
        this.title = text.getTitle();
        this.content = text.getContent();
    }
}
