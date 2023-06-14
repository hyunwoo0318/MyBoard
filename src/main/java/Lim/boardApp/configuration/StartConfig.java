package Lim.boardApp.configuration;

import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.UploadFile;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.UploadFileRepository;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.validation.valueextraction.ExtractedValue;
import java.io.File;

@Component
@RequiredArgsConstructor
public class StartConfig {

    private final UploadFileRepository uploadFileRepository;
    private final BoardRepository boardRepository;
//
//    @Value("${file.dir}")
//    private String dirPath;
//    @EventListener(ApplicationReadyEvent.class)
//    public void setDataWhenStart(){
//
//        File dir = new File(dirPath);
//        File[] fileList = dir.listFiles();
//        for (File file : fileList) {
//            UploadFile uploadFile = uploadFileRepository.fileToUploadFile(file);
//            if (uploadFile != null) {
//                uploadFileRepository.save(uploadFile);
//            }
//        }
//    }

    @Bean
    public void init(){
        if (boardRepository.findByName("soccer").isEmpty()) {
            boardRepository.save(new Board("soccer"));
            boardRepository.save(new Board("basketball"));
        }
    }
}
