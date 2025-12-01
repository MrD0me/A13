package com.example.db_setup.service;

import com.example.db_setup.model.Suggestion;
import com.example.db_setup.model.SuggestionDifficulty;
import com.example.db_setup.model.dto.suggestion.SuggestionImportItemDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionImportRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionResponseDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
import com.example.db_setup.model.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestisce la logica per l'import e il recupero dei suggerimenti.
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    /**
     * Traccia gli ID dei suggerimenti gi� mostrati per partita/classe/difficolt� cos� da non ripeterli.
     */
    private final ConcurrentHashMap<String, Set<Long>> deliveredSuggestions = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public SuggestionAvailabilityResponseDTO getAvailability(String difficultyRaw, String classNameRaw) {
        SuggestionDifficulty difficulty = mapDifficulty(difficultyRaw);
        String className = normalizeClassName(classNameRaw);
        List<Suggestion> available = fetchSuggestions(difficulty, className);
        int difficultyCap = maxForDifficulty(difficulty);
        int effectiveCap = Math.min(difficultyCap, available.size());
        // Finché nessun suggerimento è stato consumato, quelli disponibili coincidono con l'effettivo cap.
        return SuggestionAvailabilityResponseDTO.builder()
                .availableSuggestions(effectiveCap)
                .suggestionsMax(effectiveCap)
                .totalAvailableSuggestions(effectiveCap)
                .build();
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDTO requestSuggestions(SuggestionRequestDTO request) {
        SuggestionDifficulty difficulty = mapDifficulty(request.getDifficulty());
        String className = normalizeClassName(request.getClassName());
        List<Suggestion> available = fetchSuggestions(difficulty, className);

        int difficultyCap = maxForDifficulty(difficulty);
        // Limite massimo di suggerimenti considerando sia la difficoltà sia quante entry esistono davvero.
        int effectiveCap = Math.min(difficultyCap, available.size());

        String sessionKey = buildSessionKey(request.getGameId(), className, difficulty);
        // Se il client segnala un reset (es. nuova partita) azzeriamo la memoria dei suggerimenti già mostrati.
        maybeResetSession(request.getRemainingSuggestions(), effectiveCap, sessionKey);
        Set<Long> alreadyDelivered = deliveredSuggestions.computeIfAbsent(sessionKey, key -> ConcurrentHashMap.newKeySet());
        // Allineiamo il set alle entry ancora presenti nel database per evitare contatori sballati dopo un import.
        Set<Long> validSuggestionIds = available.stream()
                .map(Suggestion::getId)
                .collect(Collectors.toSet());
        alreadyDelivered.retainAll(validSuggestionIds);

        // Se abbiamo già erogato tutti i suggerimenti disponibili per questa difficoltà/classe mostriamo che siamo a zero.
        if (alreadyDelivered.size() >= effectiveCap) {
            return SuggestionResponseDTO.builder()
                    .suggestions(Collections.emptyList())
                    .remainingSuggestions(0)
                    .suggestionsAvailable(0)
                    .suggestionsMax(effectiveCap)
                    .totalAvailableSuggestions(effectiveCap)
                    .noMoreSuggestions(true)
                    .message("Non sono piu disponibili suggerimenti per questa partita.")
                    .build();
        }

        List<Suggestion> notServed = available.stream()
                .filter(suggestion -> !alreadyDelivered.contains(suggestion.getId()))
                .collect(Collectors.toList());

        if (notServed.isEmpty()) {
            return SuggestionResponseDTO.builder()
                    .suggestions(Collections.emptyList())
                    .remainingSuggestions(Math.max(effectiveCap - alreadyDelivered.size(), 0))
                    .suggestionsAvailable(Math.max(effectiveCap - alreadyDelivered.size(), 0))
                    .suggestionsMax(effectiveCap)
                    .totalAvailableSuggestions(effectiveCap)
                    .noMoreSuggestions(true)
                    .message("Non sono piu disponibili suggerimenti per questa partita.")
                    .build();
        }

        Suggestion chosen = pickRandomSuggestion(notServed);
        alreadyDelivered.add(chosen.getId());
        int deliveredCount = Math.min(alreadyDelivered.size(), effectiveCap);
        int remaining = Math.max(effectiveCap - deliveredCount, 0);
        boolean noMore = remaining == 0;

        return SuggestionResponseDTO.builder()
                .suggestions(Collections.singletonList(chosen.getText()))
                .remainingSuggestions(remaining)
                .suggestionsAvailable(remaining)
                .suggestionsMax(effectiveCap)
                .totalAvailableSuggestions(effectiveCap)
                .noMoreSuggestions(noMore)
                .message(noMore ? "Non sono piu disponibili suggerimenti per questa partita." : null)
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

    private String buildSessionKey(Long gameId, String className, SuggestionDifficulty difficulty) {
        String base = (gameId != null && gameId > 0) ? "game-" + gameId : "nogame";
        return base + "|" + className.toLowerCase() + "|" + difficulty.name();
    }

    private void maybeResetSession(Integer remainingClient, int effectiveCap, String sessionKey) {
        if (remainingClient != null && remainingClient >= effectiveCap) {
            deliveredSuggestions.remove(sessionKey);
        }
    }

    private int maxForDifficulty(SuggestionDifficulty difficulty) {
        // Numero massimo teorico per difficoltà; l'effettivo viene poi limitato dal numero reale di suggerimenti presenti.
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 5;
            case HARD -> 2;
        };
    }
}
