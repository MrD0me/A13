package com.groom.manvsclass.service;

import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;

@Service
public class SuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    /**
     * Recupera tutti i suggerimenti dal database
     * @return lista di stringhe con i suggerimenti
     */
    public List<String> getAllSuggestions() {
        List<Suggestion> suggestions = suggestionRepository.findAll();
        return suggestions.stream()
                .map(Suggestion::getText)
                .collect(Collectors.toList());
    }

    /**
     * Recupera i suggerimenti per categoria
     * @param category la categoria
     * @return lista di stringhe con i suggerimenti
     */
    public List<String> getSuggestionsByCategory(String category) {
        List<Suggestion> suggestions = suggestionRepository.findByCategory(category);
        return suggestions.stream()
                .map(Suggestion::getText)
                .collect(Collectors.toList());
    }

    /**
     * Recupera i suggerimenti per difficoltà
     * @param difficulty la difficoltà (EASY, MEDIUM, HARD)
     * @return lista di stringhe con i suggerimenti
     */
    public List<String> getSuggestionsByDifficulty(String difficulty) {
        List<Suggestion> suggestions = suggestionRepository.findByDifficulty(difficulty);
        return suggestions.stream()
                .map(Suggestion::getText)
                .collect(Collectors.toList());
    }

    /**
     * Aggiunge un nuovo suggerimento
     * @param text il testo del suggerimento
     * @param category la categoria
     * @return il suggerimento creato
     */
    public Suggestion addSuggestion(String text, String category) {
        Suggestion suggestion = new Suggestion(text, category);
        return suggestionRepository.save(suggestion);
    }

    /**
     * Elimina un suggerimento
     * @param id l'ID del suggerimento
     */
    public void deleteSuggestion(String id) {
        suggestionRepository.deleteById(id);
    }


    public String getRandomSuggestion() {
        List<Suggestion> suggestions = suggestionRepository.findAll();
        if (suggestions.isEmpty()) {
            return null; // o "" se preferisci
        }
        Random random = new Random();
        int index = random.nextInt(suggestions.size());
        return suggestions.get(index).getText();
    }
}
