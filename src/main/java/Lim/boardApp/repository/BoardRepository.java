package Lim.boardApp.repository;

import Lim.boardApp.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    public Optional<Board> findByName(String boardName);
}
