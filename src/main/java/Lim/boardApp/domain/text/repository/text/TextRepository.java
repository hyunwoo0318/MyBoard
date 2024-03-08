package Lim.boardApp.domain.text.repository.text;

import Lim.boardApp.domain.text.entity.Text;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long>, TextRepositoryCustom {
    public List<Text> findByTitle(String title) ;

}
