package Lim.boardApp.repository.comment;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Text;

import java.util.List;

public interface CommentRepositoryCustom {

    public List<Comment> findParentComments(Text text);

    public List<Comment> findCommentsByCustomer(String loginId);

    public int findCommentCnt(Long textId);
}
