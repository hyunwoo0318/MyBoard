package Lim.boardApp.form;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//페이지 블록 -> 첫번쨰 숫자와 마지막 숫자가 필요함.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBlockForm {
    private int start;
    private int end;
    private int size;
}
