package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.Board;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.HashtagRepository;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.comment.CommentRepository;
import Lim.boardApp.repository.text.TextRepository;
import Lim.boardApp.repository.texthashtag.TextHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
public class BaseService {

    protected  final TextRepository textRepository;
    protected  final CustomerRepository customerRepository;
    protected  final HashtagRepository hashtagRepository;
    protected final BoardRepository boardRepository;
    protected  final CommentRepository commentRepository;
    protected  final TextHashtagRepository textHashtagRepository;
    protected final BookmarkRepository bookmarkRepository;

    protected Text checkText(Long textId) {
        return textRepository.findById(textId).orElseThrow(()->{
            throw new NotFoundException();
        });
    }

    protected Customer checkCustomer(Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(()->{
            throw new NotFoundException();
        });
    }

    protected Board checkBoard(String boardName) {
        return boardRepository.findByName(boardName).orElseThrow(()->{
            throw new NotFoundException();
        });
    }


}
