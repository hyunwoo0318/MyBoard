package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.Bookmark;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.text.TextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TextRepository textRepository;
    private final CustomerRepository customerRepository;

    public void addBookmark(Text text, Customer customer) throws NotFoundException{

        if (text == null || customer == null) {
            throw new NotFoundException();
        }

        Bookmark bookmark = new Bookmark(text, customer);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Text text, Customer customer) throws NotFoundException {

        if (text == null || customer == null) {
            throw new NotFoundException();
        }
        bookmarkRepository.deleteBookmark(text, customer);
    }

}
