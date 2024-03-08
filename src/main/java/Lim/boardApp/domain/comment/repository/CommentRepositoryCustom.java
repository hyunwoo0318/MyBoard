package Lim.boardApp.domain.comment.repository;

import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.text.entity.Text;

import java.util.List;

public interface CommentRepositoryCustom {

    public List<Comment> findParentComments(Text text);

    public List<Comment> findCommentsByCustomer(String loginId);

    public int findCommentCnt(Long textId);

    public List<Comment> queryCommentByText(Long textId);
}
