package Lim.boardApp.repository;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Text;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.parent is NULL ")
    public List<Comment> findParentComments(Text text);

    public void deleteByText(Text text);

}
