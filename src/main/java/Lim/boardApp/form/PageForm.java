package Lim.boardApp.form;

import Lim.boardApp.domain.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class PageForm extends PageBlockForm {
    private int page;
    private int lastPage;
    private List<Text> textList;
    private String isLast;
    private String isFirst;

    public PageForm(int start, int end, int size, int page, int lastPage, List<Text> textList, boolean isLast, boolean isFirst) {
        super(start, end, size);
        this.page = page;
        this.lastPage = lastPage;
        this.textList = textList;
        this.isLast = isLast ? "Y" : "F";
        this.isFirst = isFirst ? "Y" : "F";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PageForm pageForm = (PageForm) o;

        if (page != pageForm.page) return false;
        if (lastPage != pageForm.lastPage) return false;
        if (!Objects.equals(textList, pageForm.textList)) return false;
        if (!Objects.equals(isLast, pageForm.isLast)) return false;
        return Objects.equals(isFirst, pageForm.isFirst);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + page;
        result = 31 * result + lastPage;
        result = 31 * result + (textList != null ? textList.hashCode() : 0);
        result = 31 * result + (isLast != null ? isLast.hashCode() : 0);
        result = 31 * result + (isFirst != null ? isFirst.hashCode() : 0);
        return result;
    }
}
