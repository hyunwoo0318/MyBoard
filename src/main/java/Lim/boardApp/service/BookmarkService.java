package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.Bookmark;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.BoardRepository;
import Lim.boardApp.repository.HashtagRepository;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.comment.CommentRepository;
import Lim.boardApp.repository.text.TextRepository;
import Lim.boardApp.repository.texthashtag.TextHashtagRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService extends BaseService {

    public BookmarkService(TextRepository textRepository, CustomerRepository customerRepository, HashtagRepository hashtagRepository, BoardRepository boardRepository, CommentRepository commentRepository, TextHashtagRepository textHashtagRepository, BookmarkRepository bookmarkRepository) {
        super(textRepository, customerRepository, hashtagRepository, boardRepository, commentRepository, textHashtagRepository, bookmarkRepository);
    }

    @Transactional
    public void addBookmark(Long textId, Customer customer) throws NotFoundException{

        Text text = checkText(textId);
        if (customer == null) {
            throw new NotFoundException();
        }

        Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text, customer);
        if (bookmarkOptional.isEmpty()) {
            Bookmark bookmark = new Bookmark(text, customer);
            bookmarkRepository.save(bookmark);
        }
    }

    @Transactional
    public void deleteBookmark(Long textId, Customer customer) throws NotFoundException {

        Text text = checkText(textId);
        if (customer == null) {
            throw new NotFoundException();
        }

        bookmarkRepository.deleteBookmark(text, customer);
    }

    public List<Text> findBookmarkedTextsByCustomer(String loginId){
        return bookmarkRepository.findBookmarkedTextsByCustomer(loginId);
    }

    public boolean isBookmarked(Long textId, Long customerId) {
        Text text = checkText(textId);
        Customer customer = checkCustomer(customerId);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text, customer);
        return bookmarkOptional.isPresent();
    }
}
