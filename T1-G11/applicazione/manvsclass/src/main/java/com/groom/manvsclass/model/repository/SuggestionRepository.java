package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Suggestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends MongoRepository<Suggestion, String> {

    List<Suggestion> findByCategory(String category);

    List<Suggestion> findByDifficulty(String difficulty);

    List<Suggestion> findByClassNameIgnoreCase(String className);

    List<Suggestion> findByClassNameIgnoreCaseAndDifficulty(String className, String difficulty);

    void deleteByClassNameIgnoreCase(String className);
}
