package Lim.boardApp.repository;

import Lim.boardApp.repository.comment.CommentRepository;
import Lim.boardApp.repository.text.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TextRepository textRepository;


}