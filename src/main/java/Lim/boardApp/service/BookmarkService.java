package Lim.boardApp.service;

import Lim.boardApp.Exception.NotFoundException;
import Lim.boardApp.domain.Bookmark;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;
import Lim.boardApp.repository.CustomerRepository;
import Lim.boardApp.repository.bookmark.BookmarkRepository;
import Lim.boardApp.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final TextRepository textRepository;
    private final CustomerRepository customerRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void addBookmark(Long textId, Customer customer) throws NotFoundException {

        Text text =
                textRepository
                        .findById(textId)
                        .orElseThrow(
                                () -> {
                                    throw new NotFoundException();
                                });
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

        Text text =
                textRepository
                        .findById(textId)
                        .orElseThrow(
                                () -> {
                                    throw new NotFoundException();
                                });
        if (customer == null) {
            throw new NotFoundException();
        }

        bookmarkRepository.deleteBookmark(text, customer);
    }

    public List<Text> findBookmarkedTextsByCustomer(String loginId) {
        return bookmarkRepository.findBookmarkedTextsByCustomer(loginId);
    }

    public boolean isBookmarked(Long textId, Long customerId) {
        Text text =
                textRepository
                        .findById(textId)
                        .orElseThrow(
                                () -> {
                                    throw new NotFoundException();
                                });
        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(
                                () -> {
                                    throw new NotFoundException();
                                });
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.queryBookmark(text, customer);
        return bookmarkOptional.isPresent();
    }
}
