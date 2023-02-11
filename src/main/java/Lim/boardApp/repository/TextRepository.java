package Lim.boardApp.repository;

import Lim.boardApp.domain.Text;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long> {
    public List<Text> findByTitle(String title) ;

    @Query("select t from Text t where t.content like %:searchKey% or t.title like %:searchKey%")
    public Page<Text> searchTextByContentTitle(@Param("searchKey") String searchKey, Pageable pageable);


    @Query("select t from Text t where t.content like %:searchKey%")
    public Page<Text> searchTextByContent(@Param("searchKey") String searchKey, Pageable pageable);

    @Query("select t from Text t where t.title like %:searchKey%")
    public Page<Text> searchTextByTitle(@Param("searchKey") String searchKey, Pageable pageable);
}
