package Lim.boardApp.repository.text;

import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long>, TextRepositoryCustom {
    public List<Text> findByTitle(String title) ;

    public Page<Text> findByBoard(Board board, Pageable pageable);
}
