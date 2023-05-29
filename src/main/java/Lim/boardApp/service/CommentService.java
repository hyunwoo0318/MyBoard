package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.form.CommentForm;
import Lim.boardApp.repository.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TextService textService;

    public List<Comment> findParentCommentList(Text text) {
        return commentRepository.findParentComments(text);
    }

    public void addComment(Text text, Customer customer, CommentForm commentForm) throws NotFoundException {

        Comment comment = null;
        if (commentForm.getParent() != null) {
            Comment parent = commentRepository.findById(commentForm.getParent()).orElseThrow(() -> {
                throw new NotFoundException();
            });

            comment = new Comment(text, customer, commentForm.getContent(), parent);
            comment.setChildCommentList(parent);
        }else{
            comment = new Comment(text, customer, commentForm.getContent());
        }
        commentRepository.save(comment);
    }

    public List<Comment> findCommentsByCustomer(String loginId) {
        return commentRepository.findCommentsByCustomer(loginId);
    }
}
