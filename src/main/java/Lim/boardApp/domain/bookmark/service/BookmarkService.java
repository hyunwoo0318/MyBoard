package Lim.boardApp.domain.bookmark.service;

import Lim.boardApp.common.exception.NotFoundException;
import Lim.boardApp.domain.bookmark.entity.Bookmark;
import Lim.boardApp.domain.bookmark.repository.BookmarkRepository;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.entity.Text;
import Lim.boardApp.domain.text.repository.text.TextRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final TextRepository textRepository;
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
}
