package Lim.boardApp.repository;

import Lim.boardApp.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    public Hashtag findByName(String name);
}
