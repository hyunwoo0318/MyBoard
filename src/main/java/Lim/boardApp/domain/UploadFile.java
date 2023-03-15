package Lim.boardApp.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
public class UploadFile {
    private Long id;


    private String storedFileName;

    public void setId(Long id) {
        this.id = id;
    }

    @Builder
    public UploadFile( String storedFileName) {

        this.storedFileName = storedFileName;
    }

    public UploadFile() {
    }
}
