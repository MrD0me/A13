package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Suggestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends MongoRepository<Suggestion, String> {
    /**
     * Restituisce tutti i suggerimenti di una determinata categoria
     * @param category la categoria dei suggerimenti
     * @return lista di suggerimenti
     */
    List<Suggestion> findByCategory(String category);

    /**
     * Restituisce tutti i suggerimenti di una determinata difficoltà
     * @param difficulty la difficoltà (EASY, MEDIUM, HARD)
     * @return lista di suggerimenti
     */
    List<Suggestion> findByDifficulty(String difficulty);
}
