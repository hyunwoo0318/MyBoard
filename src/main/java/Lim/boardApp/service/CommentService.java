package Lim.boardApp.service;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public List<Comment> findCommentList(Text text) {
        return commentRepository.findCommentsByText(text);
    }

    public void addComment(Text text, Customer customer, String commentContent) {
        Comment comment = new Comment(text, customer, commentContent);
        commentRepository.save(comment);
    }
}
