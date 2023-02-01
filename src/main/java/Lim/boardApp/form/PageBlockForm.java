package Lim.boardApp.form;


import lombok.Data;

//페이지 블록 -> 첫번쨰 숫자와 마지막 숫자가 필요함.
@Data
public class PageBlockForm {
    private final int start;
    private final int end;
    private final int size;
}
