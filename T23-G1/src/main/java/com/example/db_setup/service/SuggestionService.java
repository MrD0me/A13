package com.example.db_setup.service;

import com.example.db_setup.model.Suggestion;
import com.example.db_setup.model.SuggestionDifficulty;
import com.example.db_setup.model.dto.suggestion.SuggestionImportItemDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionImportRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionResponseDTO;
import com.example.db_setup.model.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Gestisce la logica per l'import e il recupero dei suggerimenti.
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;

    @Transactional(readOnly = true)
    public SuggestionResponseDTO requestSuggestions(SuggestionRequestDTO request) {
        SuggestionDifficulty difficulty = mapDifficulty(request.getDifficulty());
        String className = normalizeClassName(request.getClassName());
        List<Suggestion> available = fetchSuggestions(difficulty, className);

        Suggestion chosen = pickRandomSuggestion(available);
        int remaining = computeRemainingSuggestions(request.getRemainingSuggestions());
        boolean noMore = remaining == 0;

        return SuggestionResponseDTO.builder()
                .suggestions(Collections.singletonList(chosen.getText()))
                .remainingSuggestions(remaining)
                .noMoreSuggestions(noMore)
                .message(noMore ? "Non hai piu suggerimenti disponibili per questa partita." : null)
                .build();
    }

    @Transactional
    public void replaceSuggestions(SuggestionImportRequestDTO request) {
        String normalizedClassName = normalizeClassName(request.getClassName());
        List<SuggestionImportItemDTO> payload = request.getSuggestions();
        if (payload == null || payload.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nessun suggerimento fornito");
        }

        suggestionRepository.deleteByClassNameIgnoreCase(normalizedClassName);
        List<Suggestion> toSave = payload.stream()
                .map(item -> buildSuggestion(normalizedClassName, item))
                .collect(Collectors.toList());
        suggestionRepository.saveAll(toSave);
    }

    private SuggestionDifficulty mapDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La difficolta e obbligatoria");
        }
        try {
            return SuggestionDifficulty.fromString(difficulty);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private List<Suggestion> fetchSuggestions(SuggestionDifficulty difficulty, String className) {
        List<Suggestion> filtered = suggestionRepository.findByDifficultyAndClassNameIgnoreCase(difficulty, className);
        if (filtered.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Nessun suggerimento disponibile per la difficolta " + difficulty + " e classe " + className);
        }
        return filtered;
    }

    private Suggestion buildSuggestion(String className, SuggestionImportItemDTO item) {
        SuggestionDifficulty difficulty = mapDifficulty(item.getDifficulty());
        String text = normalizeText(item.getText());
        return Suggestion.builder()
                .className(className)
                .difficulty(difficulty)
                .text(text)
                .language("it")
                .build();
    }

    private String normalizeClassName(String className) {
        if (!StringUtils.hasText(className)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La classe di riferimento e obbligatoria");
        }
        return className.trim();
    }

    private String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Il testo del suggerimento non puo essere vuoto");
        }
        return text.trim();
    }

    private Suggestion pickRandomSuggestion(List<Suggestion> suggestions) {
        if (suggestions.size() == 1) {
            return suggestions.get(0);
        }
        int index = ThreadLocalRandom.current().nextInt(suggestions.size());
        return suggestions.get(index);
    }

    private int computeRemainingSuggestions(Integer clientRemaining) {
        if (clientRemaining == null || clientRemaining <= 0) {
            return 0;
        }
        int updatedValue = clientRemaining - 1;
        return Math.max(updatedValue, 0);
    }
}
