package Lim.boardApp.repository;

import Lim.boardApp.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    public Optional<Hashtag> findByName(String name);
}
