package Lim.boardApp.domain.bookmark.repository;

import Lim.boardApp.domain.bookmark.entity.Bookmark;
import Lim.boardApp.domain.customer.entity.Customer;
import Lim.boardApp.domain.text.entity.Text;

import java.util.Optional;

public interface BookmarkRepositoryCustom {

    public void deleteBookmark(Text text, Customer customer);


    public Optional<Bookmark> queryBookmark(Text text, Customer customer);
}
