package Lim.boardApp.repository;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Text;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findCommentsByText(Text text);
    public void deleteByText(Text text);

}
