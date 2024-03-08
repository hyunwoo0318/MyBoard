package Lim.boardApp.domain.text.repository.board;

import Lim.boardApp.domain.text.entity.Board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    public Optional<Board> findByName(String boardName);
}
