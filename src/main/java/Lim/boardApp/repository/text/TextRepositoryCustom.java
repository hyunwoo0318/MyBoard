package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextRepositoryCustom {

    public List<Text> searchTextByContentTitle(String searchKey);
    public List<Text> searchTextByContentTitle(String searchKey,String boardName);



    public List<Text> searchTextByContent(String searchKey);
    public List<Text> searchTextByContent(String searchKey,String boardName);

    public List<Text> searchTextByTitle(String searchKey,String boardName);
    public List<Text> searchTextByTitle(String searchKey);

    public List<Text> queryTextByCustomer(String loginId);

    public Long updateViewCount(Long textId, Long viewCnt);

}
