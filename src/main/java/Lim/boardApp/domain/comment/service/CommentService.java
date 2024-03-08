package Lim.boardApp.domain.comment.service;

import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.domain.comment.entity.Comment;
import Lim.boardApp.domain.comment.form.CommentCreateForm;
import Lim.boardApp.domain.comment.repository.CommentRepository;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TextRepository textRepository;

    @Transactional
    public void addComment(Customer customer, CommentCreateForm commentCreateForm, Long textId)
        throws NotFoundException {
        Comment comment = null;
        Long parentCommentId = commentCreateForm.getParent();
        if (parentCommentId != null) {
            Comment parentComment = new Comment(parentCommentId);
            comment = new Comment(new Text(textId), customer, commentCreateForm.getContent(), parentComment);
        } else {
            comment = new Comment(new Text(textId), customer, commentCreateForm.getContent());
        }
        commentRepository.save(comment);
    }


}
