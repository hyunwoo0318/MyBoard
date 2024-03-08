package Lim.boardApp.domain.text.repository.hashtag;

import Lim.boardApp.domain.text.entity.Hashtag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    public List<Hashtag> findByNameIn(List<String> nameList);
}
