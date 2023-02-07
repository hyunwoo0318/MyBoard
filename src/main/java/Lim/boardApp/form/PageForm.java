package Lim.boardApp.form;

import Lim.boardApp.domain.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
public class PageForm extends PageBlockForm {
    private int page;
    private int lastPage;
    private List<Text> textList;
    private boolean isLast;
    private boolean isFirst;

    public PageForm(int start, int end, int size, int page, int lastPage, List<Text> textList, boolean isLast, boolean isFirst) {
        super(start, end, size);
        this.page = page;
        this.lastPage = lastPage;
        this.textList = textList;
        this.isLast = isLast;
        this.isFirst = isFirst;
    }


}
