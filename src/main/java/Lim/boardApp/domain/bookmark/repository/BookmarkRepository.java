package Lim.boardApp.domain.bookmark.repository;

import Lim.boardApp.domain.bookmark.entity.Bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>,
    BookmarkRepositoryCustom {

}
