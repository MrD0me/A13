package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.ClassUT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassUT, String> {

    @Query("select c from ClassUT c where lower(c.name) like lower(concat('%', :text, '%'))")
    List<ClassUT> searchByName(@Param("text") String text);

    @Query("select distinct c from ClassUT c join c.category cat where lower(cat) = lower(:category)")
    List<ClassUT> findByCategoryIgnoreCase(@Param("category") String category);

    @Query("select distinct c from ClassUT c join c.category cat where lower(cat) = lower(:category) and " +
            "(lower(c.name) like lower(concat('%', :text, '%')) or lower(c.description) like lower(concat('%', :text, '%')))")
    List<ClassUT> searchByTextAndCategory(@Param("text") String text, @Param("category") String category);

    List<ClassUT> findByDifficulty(String difficulty);

    List<ClassUT> findAllByOrderByDateAsc();

    List<ClassUT> findAllByOrderByNameAsc();

    @Query("select c from ClassUT c where (lower(c.name) like lower(concat('%', :text, '%')) or lower(c.description) like lower(concat('%', :text, '%'))) and c.difficulty = :difficulty")
    List<ClassUT> searchByTextAndDifficulty(@Param("text") String text, @Param("difficulty") String difficulty);
}
