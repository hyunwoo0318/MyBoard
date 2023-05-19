package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextRepositoryCustom {

    public Page<Text> searchTextByContentTitle(String searchKey, Pageable pageable);

    public Page<Text> searchTextByContent(String searchKey, Pageable pageable);

    public Page<Text> searchTextByTitle(String searchKey, Pageable pageable);

    public List<Text> queryTextByCustomer(String loginId);

    public Long updateViewCount(Long textId, Long viewCnt);

}
