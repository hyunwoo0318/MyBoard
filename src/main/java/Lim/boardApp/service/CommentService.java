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
            Optional<Comment> commentOptional = commentRepository.findById(commentForm.getParent());
            if(commentOptional.isEmpty()){
                throw new NotFoundException();
            }
            Comment parent = commentOptional.get();
            comment = new Comment(text, customer, commentForm.getContent(), parent);
            comment.setChildCommentList(parent);
        }else{
            comment = new Comment(text, customer, commentForm.getContent());
        }
        commentRepository.save(comment);
    }
}
