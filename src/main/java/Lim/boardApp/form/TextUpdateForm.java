package Lim.boardApp.form;


import lombok.Data;

//글 수정시 제목과 내용만 바뀔수 있게 함
@Data
public class TextUpdateForm {

    private String title;
    private String content;
}
