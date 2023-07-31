package Lim.boardApp.repository.bookmark;

import Lim.boardApp.domain.Bookmark;
import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepositoryCustom {

    public void deleteBookmark(Text text, Customer customer);

    public List<Text> findBookmarkedTextsByCustomer(String loginId);

    public Optional<Bookmark> queryBookmark(Text text, Customer customer);
}
