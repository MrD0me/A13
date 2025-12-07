package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.SuggestionDifficulty;
import com.groom.manvsclass.model.SuggestionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByDifficultyAndClassNameIgnoreCase(SuggestionDifficulty difficulty, String className);

    List<Suggestion> findByDifficultyAndClassNameIgnoreCaseAndTier(SuggestionDifficulty difficulty, String className, SuggestionTier tier);

    void deleteByClassNameIgnoreCase(String className);
}
