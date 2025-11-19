package com.example.db_setup.model.repository;

import com.example.db_setup.model.Suggestion;
import com.example.db_setup.model.SuggestionDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByDifficultyAndClassNameIgnoreCase(SuggestionDifficulty difficulty, String className);

    void deleteByClassNameIgnoreCase(String className);
}
