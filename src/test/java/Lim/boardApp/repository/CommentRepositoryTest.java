package Lim.boardApp.repository;

import Lim.boardApp.domain.Comment;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired CommentRepository commentRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired TextRepository textRepository;

    @Test
    @DisplayName("findCommentsByText 테스트")
    public void findCommentsByTextTest(){
        Customer customer = new Customer();
        customerRepository.save(customer);

        Text text = new Text("content123", "title123", customer);
        textRepository.save(text);

        Comment comment1 = new Comment(text,customer,"commentContent111");
        Comment comment2 = new Comment(text,customer,"commentContent222");
        Comment comment3 = new Comment(text,customer,"commentContent333");
        List<Comment> commentList = Arrays.asList(comment1, comment2,comment3);
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);

        List<Comment> result = commentRepository.findCommentsByText(text);

        assertThat(result.size()).isEqualTo(3);
        assertThat(result).isEqualTo(commentList);
    }
}