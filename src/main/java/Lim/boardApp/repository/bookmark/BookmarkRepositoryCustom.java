package Lim.boardApp.repository.bookmark;

import Lim.boardApp.domain.Customer;
import Lim.boardApp.domain.Text;

public interface BookmarkRepositoryCustom {

    public void deleteBookmark(Text text, Customer customer);
}
