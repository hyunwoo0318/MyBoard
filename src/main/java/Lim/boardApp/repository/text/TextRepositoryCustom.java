package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface TextRepositoryCustom {

    public Page<Text> searchTextByContentTitle(String searchKey, Pageable pageable);

    public Page<Text> searchTextByContent(String searchKey, Pageable pageable);

    public Page<Text> searchTextByTitle(String searchKey, Pageable pageable);
}
