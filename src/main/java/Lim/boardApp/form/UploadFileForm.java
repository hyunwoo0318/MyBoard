package Lim.boardApp.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileForm {
    private Long id;
    private Long userId;
    private MultipartFile multipartFile;
}
