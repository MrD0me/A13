package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, String> {

    List<Suggestion> findByCategory(String category);

    List<Suggestion> findByDifficulty(String difficulty);
}
