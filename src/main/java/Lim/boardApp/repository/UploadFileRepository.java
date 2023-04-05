package Lim.boardApp.repository;

import Lim.boardApp.domain.UploadFile;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UploadFileRepository {
    private final Map<Long, UploadFile> m = new HashMap<>();
    private static Long sequence = 0L;

    public void save(UploadFile uploadFile) {
        sequence++;
        uploadFile.setId(sequence);
        m.put(sequence, uploadFile);
    }

    public UploadFile fileToUploadFile(File file){
        if(!file.exists()) return null;
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('|');
        if(idx == -1) return null;
        long userId = Long.parseLong(fileName.substring(0, idx));
        return new UploadFile(fileName);
    }


    public UploadFile findById(Long id) {
        return m.get(id);
    }


}
