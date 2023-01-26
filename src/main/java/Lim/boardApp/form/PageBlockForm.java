package Lim.boardApp.form;

import lombok.Builder;
import lombok.Data;

//페이지 블록 -> 첫번쨰 숫자와 마지막 숫자가 필요함.
@Data
@Builder
public class PageBlockForm {
    public int start;
    public int end;
    public int length;
}
