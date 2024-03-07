package Lim.boardApp.configuration;

import Lim.boardApp.constant.RoleConst;
import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.UploadFileRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartConfig {

    private final UploadFileRepository uploadFileRepository;
    private final BoardRepository boardRepository;
    private final CustomerRepository customerRepository;
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
        if (boardRepository.findByName("축구").isEmpty()) {
            boardRepository.save(new Board("축구"));
            boardRepository.save(new Board("야구"));
            boardRepository.save(new Board("배구"));
            boardRepository.save(new Board("골프"));
            boardRepository.save(new Board("농구"));
        }
        if (customerRepository.findByName("auto-bot").isEmpty()) {
            Customer customer = new Customer("cus1", "cus1", "auto-bot", null, RoleConst.USER, null, "ex@naver.com");
            customerRepository.save(customer);
        }
    }
}
