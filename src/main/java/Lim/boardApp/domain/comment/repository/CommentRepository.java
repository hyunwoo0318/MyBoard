package Lim.boardApp.domain.comment.repository;

import Lim.boardApp.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

}
