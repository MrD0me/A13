package com.example.db_setup.service;

import com.example.db_setup.model.Suggestion;
import com.example.db_setup.model.SuggestionDifficulty;
import com.example.db_setup.model.SuggestionTier;
import com.example.db_setup.model.dto.suggestion.AdvancedSuggestionRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Gestisce la logica per l'import e il recupero dei suggerimenti.
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private static final int DEFAULT_ADVANCED_COST = 2;

    private final SuggestionRepository suggestionRepository;
    private final PlayerProgressService playerProgressService;
    /**
     * Traccia gli ID dei suggerimenti già mostrati per partita/classe/difficoltà così da non ripeterli.
     */
    private final ConcurrentHashMap<String, Set<Long>> deliveredSuggestions = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public SuggestionAvailabilityResponseDTO getAvailability(String difficultyRaw, String classNameRaw, String tierRaw) {
        SuggestionDifficulty difficulty = mapDifficulty(difficultyRaw);
        SuggestionTier tier = mapTier(tierRaw);
        String className = normalizeClassName(classNameRaw);
        List<Suggestion> available = suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(difficulty, className, tier);

        if (available.isEmpty()) {
            return SuggestionAvailabilityResponseDTO.builder()
                    .availableSuggestions(0)
                    .suggestionsMax(0)
                    .totalAvailableSuggestions(0)
                    .build();
        }

        int effectiveCap = capForTier(difficulty, tier, available.size());
        // Il numero di suggerimenti disponibili coincide con i suggerimenti realmente utilizzabili.
        return SuggestionAvailabilityResponseDTO.builder()
                .availableSuggestions(effectiveCap)
                .suggestionsMax(effectiveCap)
                .totalAvailableSuggestions(effectiveCap)
                .build();
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDTO requestSuggestions(SuggestionRequestDTO request) {
        return requestSuggestionsInternal(request, SuggestionTier.BASE, null, null);
    }

    @Transactional
    public SuggestionResponseDTO requestAdvancedSuggestions(AdvancedSuggestionRequestDTO request) {
        return requestSuggestionsInternal(request, SuggestionTier.ADVANCED, request.getPlayerId(), request.getCost());
    }

    private SuggestionResponseDTO requestSuggestionsInternal(SuggestionRequestDTO request,
                                                             SuggestionTier tier,
                                                             Long playerId,
                                                             Integer requestedCost) {
        SuggestionDifficulty difficulty = mapDifficulty(request.getDifficulty());
        String className = normalizeClassName(request.getClassName());
        List<Suggestion> available = suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(difficulty, className, tier);

        if (available.isEmpty() && tier == SuggestionTier.ADVANCED) {
            return buildEmptyResponse(0, 0, tier, "Nessun suggerimento avanzato disponibile per questa combinazione.");
        }
        if (available.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Nessun suggerimento disponibile per la difficolta " + difficulty + " e classe " + className + " nel tier " + tier);
        }

        int effectiveCap = capForTier(difficulty, tier, available.size());
        String sessionKey = buildSessionKey(request.getGameId(), className, difficulty, tier);

        // Se il client segnala un reset (es. nuova partita) azzeriamo la memoria dei suggerimenti già mostrati.
        maybeResetSession(request.getRemainingSuggestions(), effectiveCap, sessionKey, request.getGameId());
        Set<Long> alreadyDelivered = deliveredSuggestions.computeIfAbsent(sessionKey, key -> ConcurrentHashMap.newKeySet());
        // Allineiamo il set alle entry ancora presenti nel database per evitare contatori sballati dopo un import.
        Set<Long> validSuggestionIds = available.stream()
                .map(Suggestion::getId)
                .collect(Collectors.toSet());
        alreadyDelivered.retainAll(validSuggestionIds);

        SuggestionSelection selection = selectSuggestion(available, alreadyDelivered, effectiveCap);
        if (selection.suggestion == null) {
            return buildEmptyResponse(effectiveCap, selection.remainingAfterPick, tier,
                    "Non sono piu disponibili suggerimenti per questa partita.");
        }

        int cost = (requestedCost != null && requestedCost > 0) ? requestedCost : DEFAULT_ADVANCED_COST;
        Integer creditsLeft = null;
        Integer creditsSpent = null;
        if (tier == SuggestionTier.ADVANCED) {
            if (playerId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerId obbligatorio per i suggerimenti avanzati");
            }
            creditsLeft = playerProgressService.spendHintCredits(playerId, cost);
            creditsSpent = cost;
        }

        alreadyDelivered.add(selection.suggestion.getId());
        int remaining = Math.max(effectiveCap - alreadyDelivered.size(), 0);
        boolean noMore = remaining == 0;

        return SuggestionResponseDTO.builder()
                .suggestions(Collections.singletonList(selection.suggestion.getText()))
                .remainingSuggestions(remaining)
                .suggestionsAvailable(remaining)
                .suggestionsMax(effectiveCap)
                .totalAvailableSuggestions(effectiveCap)
                .noMoreSuggestions(noMore)
                .message(noMore ? "Non sono piu disponibili suggerimenti per questa partita." : null)
                .creditsLeft(creditsLeft)
                .creditsSpent(creditsSpent)
                .suggestionCost(tier == SuggestionTier.ADVANCED ? cost : null)
                .tier(tier.name())
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

    private SuggestionTier mapTier(String tierRaw) {
        try {
            return SuggestionTier.fromString(tierRaw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private Suggestion buildSuggestion(String className, SuggestionImportItemDTO item) {
        SuggestionDifficulty difficulty = mapDifficulty(item.getDifficulty());
        SuggestionTier tier = mapTier(item.getTier());
        String text = normalizeText(item.getText());
        return Suggestion.builder()
                .className(className)
                .difficulty(difficulty)
                .tier(tier)
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

    private String buildSessionKey(Long gameId, String className, SuggestionDifficulty difficulty, SuggestionTier tier) {
        String base = (gameId != null && gameId > 0) ? "game-" + gameId : "nogame";
        return base + "|" + className.toLowerCase() + "|" + difficulty.name() + "|" + tier.name();
    }

    private void maybeResetSession(Integer remainingClient, int effectiveCap, String sessionKey, Long gameId) {
        // Se la partita ha un identificativo valido, usiamo sempre lo stato server-side senza fidarci del contatore client.
        if (gameId != null && gameId > 0) {
            return;
        }
        if (remainingClient != null && remainingClient >= effectiveCap) {
            deliveredSuggestions.remove(sessionKey);
        }
    }

    private int capForTier(SuggestionDifficulty difficulty, SuggestionTier tier, int availableCount) {
        if (tier == SuggestionTier.ADVANCED) {
            return availableCount;
        }
        return Math.min(maxForDifficulty(difficulty), availableCount);
    }

    private int maxForDifficulty(SuggestionDifficulty difficulty) {
        // Numero massimo teorico per difficoltà; l'effettivo viene poi limitato dal numero reale di suggerimenti presenti.
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 5;
            case HARD -> 2;
        };
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Integer> getCaps() {
        return java.util.Map.of(
                SuggestionDifficulty.EASY.name(), maxForDifficulty(SuggestionDifficulty.EASY),
                SuggestionDifficulty.MEDIUM.name(), maxForDifficulty(SuggestionDifficulty.MEDIUM),
                SuggestionDifficulty.HARD.name(), maxForDifficulty(SuggestionDifficulty.HARD)
        );
    }

    private SuggestionSelection selectSuggestion(List<Suggestion> available, Set<Long> alreadyDelivered, int effectiveCap) {
        if (alreadyDelivered.size() >= effectiveCap) {
            return new SuggestionSelection(null, 0);
        }

        List<Suggestion> notServed = available.stream()
                .filter(suggestion -> !alreadyDelivered.contains(suggestion.getId()))
                .collect(Collectors.toList());

        if (notServed.isEmpty()) {
            int remaining = Math.max(effectiveCap - alreadyDelivered.size(), 0);
            return new SuggestionSelection(null, remaining);
        }

        Suggestion chosen = pickRandomSuggestion(notServed);
        int deliveredCount = Math.min(alreadyDelivered.size() + 1, effectiveCap);
        int remaining = Math.max(effectiveCap - deliveredCount, 0);
        return new SuggestionSelection(chosen, remaining);
    }

    private SuggestionResponseDTO buildEmptyResponse(int effectiveCap, int remaining, SuggestionTier tier, String message) {
        return SuggestionResponseDTO.builder()
                .suggestions(Collections.emptyList())
                .remainingSuggestions(remaining)
                .suggestionsAvailable(remaining)
                .suggestionsMax(effectiveCap)
                .totalAvailableSuggestions(effectiveCap)
                .noMoreSuggestions(true)
                .message(message)
                .tier(tier.name())
                .build();
    }

    private static final class SuggestionSelection {
        private final Suggestion suggestion;
        private final int remainingAfterPick;

        private SuggestionSelection(Suggestion suggestion, int remainingAfterPick) {
            this.suggestion = suggestion;
            this.remainingAfterPick = remainingAfterPick;
        }
    }
}
