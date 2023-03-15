package Lim.boardApp.service;

import Lim.boardApp.domain.UploadFile;
import Lim.boardApp.repository.UploadFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadFileService {
    @Value("${file.dir}")
    private String fileDir;

    private final UploadFileRepository uploadFileRepository;


    public String setFullFilePath(String storedFileName){
        return fileDir + storedFileName;
    }
    public String setStoredFileName(String originalFileName) {
        int idx = originalFileName.lastIndexOf('.');
        String ext = originalFileName.substring(idx + 1);

        String uuid = UUID.randomUUID().toString();
        String storedFileName = uuid + '.' + ext;
        return storedFileName;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = setStoredFileName(originalFilename);
        multipartFile.transferTo(new File(setFullFilePath(storedFileName)));
        UploadFile uploadFile = new UploadFile(storedFileName);
        uploadFileRepository.save(uploadFile);
        return uploadFile;
    }

    public List<UploadFile> storeFiles(Map<Long, MultipartFile> multipartFileMap) throws IOException {
        List<UploadFile> storeFileList = new ArrayList<>();
        for (Map.Entry<Long, MultipartFile> entry : multipartFileMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                UploadFile uploadFile = storeFile(entry.getValue());
                storeFileList.add(uploadFile);
                uploadFileRepository.save(uploadFile);
            }
        }
        return storeFileList;
    }
}
