package com.groom.manvsclass.service;

import com.groom.manvsclass.api.UserServiceClient;
import com.groom.manvsclass.model.DeliveredSuggestion;
import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.SuggestionDifficulty;
import com.groom.manvsclass.model.SuggestionTier;
import com.groom.manvsclass.model.dto.suggestion.AdvancedSuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionListItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionImportItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionImportRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionResponseDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionCreateRequestDTO;
import com.groom.manvsclass.model.repository.DeliveredSuggestionRepository;
import com.groom.manvsclass.model.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Gestisce la logica per l'import e il recupero dei suggerimenti.
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    //variabile di fallback
    //la vera variabile che controlla il costo dei suggerimenti avanzati si trova
    //in Suggestion.js (T5)
    private static final int DEFAULT_ADVANCED_COST = 2;

    private final SuggestionRepository suggestionRepository;
    private final UserServiceClient userServiceClient;
    private final DeliveredSuggestionRepository deliveredSuggestionRepository;

    @Transactional(readOnly = true)
    public SuggestionAvailabilityResponseDTO getAvailability(String difficultyRaw, String classNameRaw, String tierRaw, Long gameId) {
        SuggestionRequestContext context = buildContext(difficultyRaw, classNameRaw, tierRaw);
        List<Suggestion> available = findSuggestions(context);

        if (available.isEmpty()) {
            return availabilityResponse(0, 0, Collections.emptyList());
        }

        int effectiveCap = capForTier(context.difficulty(), context.tier(), available.size());
        String sessionKey = buildSessionKey(gameId, context.className(), context.difficulty(), context.tier());
        Set<Long> delivered = loadDeliveredSuggestions(sessionKey);
        java.util.Map<Long, String> textById = available.stream()
                .collect(Collectors.toMap(Suggestion::getId, Suggestion::getText, (a, b) -> a, java.util.LinkedHashMap::new));
        Set<Long> validSuggestionIds = new java.util.HashSet<>(textById.keySet());
        delivered = purgeInvalidDelivered(sessionKey, delivered, validSuggestionIds);

        List<String> deliveredTexts = delivered.isEmpty()
                ? Collections.emptyList()
                : delivered.stream()
                .map(textById::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        int remaining = Math.max(effectiveCap - delivered.size(), 0);
        return availabilityResponse(remaining, effectiveCap, deliveredTexts);
    }

    @Transactional
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
        SuggestionRequestContext context = buildContext(request.getDifficulty(), request.getClassName(), tier);
        List<Suggestion> available = findSuggestions(context);

        if (available.isEmpty() && context.isAdvanced()) {
            return buildEmptyResponse(0, 0, context.tier(), "Nessun suggerimento avanzato disponibile per questa combinazione.");
        }
        if (available.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Nessun suggerimento disponibile per la difficolta " + context.difficulty()
                            + " e classe " + context.className() + " nel tier " + context.tier());
        }

        int effectiveCap = capForTier(context.difficulty(), context.tier(), available.size());
        String sessionKey = buildSessionKey(request.getGameId(), context.className(), context.difficulty(), context.tier());

        // Se il client segnala un reset (es. nuova partita) azzeriamo la memoria dei suggerimenti gia mostrati.
        maybeResetSession(request.getRemainingSuggestions(), effectiveCap, sessionKey, request.getGameId());
        Set<Long> alreadyDelivered = loadDeliveredSuggestions(sessionKey);
        // Allineiamo il set alle entry ancora presenti nel database per evitare contatori sballati dopo un import.
        Set<Long> validSuggestionIds = available.stream()
                .map(Suggestion::getId)
                .collect(Collectors.toSet());
        alreadyDelivered = purgeInvalidDelivered(sessionKey, alreadyDelivered, validSuggestionIds);

        SuggestionSelection selection = selectSuggestion(available, alreadyDelivered, effectiveCap);
        if (selection.suggestion == null) {
            return buildEmptyResponse(effectiveCap, selection.remainingAfterPick, context.tier(),
                    "Non sono piu disponibili suggerimenti per questa partita.");
        }

        int cost = determineAdvancedCost(requestedCost);
        Integer creditsLeft = null;
        Integer creditsSpent = null;
        if (context.isAdvanced()) {
            if (playerId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerId obbligatorio per i suggerimenti avanzati");
            }
            creditsLeft = userServiceClient.spendHintCredits(playerId, cost);
            creditsSpent = cost;
        }

        alreadyDelivered.add(selection.suggestion.getId());
        persistDeliveredSuggestion(sessionKey, selection.suggestion.getId());
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
                .suggestionCost(context.isAdvanced() ? cost : null)
                .tier(context.tier().name())
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

    @Transactional
    public Suggestion createSuggestion(SuggestionCreateRequestDTO request) {
        String normalizedClassName = normalizeClassName(request.getClassName());
        Suggestion newSuggestion = Suggestion.builder()
                .className(normalizedClassName)
                .difficulty(mapDifficulty(request.getDifficulty()))
                .tier(mapTier(request.getTier()))
                .text(normalizeText(request.getText()))
                .language(StringUtils.hasText(request.getLanguage()) ? request.getLanguage().trim() : "it")
                .build();
        return suggestionRepository.save(newSuggestion);
    }

    @Transactional(readOnly = true)
    public List<SuggestionListItemDTO> listSuggestions(String classNameRaw, String difficultyRaw, String tierRaw) {
        SuggestionDifficulty difficulty = mapDifficulty(difficultyRaw);
        String className = normalizeClassName(classNameRaw);
        List<Suggestion> results;
        if (StringUtils.hasText(tierRaw)) {
            SuggestionTier tier = mapTier(tierRaw);
            results = findSuggestions(new SuggestionRequestContext(className, difficulty, tier));
        } else {
            results = suggestionRepository.findByClassNameIgnoreCaseAndDifficulty(className, difficulty);
        }
        return results.stream()
                .map(s -> new SuggestionListItemDTO(
                        s.getId(),
                        s.getText(),
                        s.getClassName(),
                        s.getDifficulty().name(),
                        s.getTier() != null ? s.getTier().name() : SuggestionTier.BASE.name(),
                        s.getLanguage()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSuggestion(Long id) {
        if (!suggestionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggerimento non trovato");
        }
        suggestionRepository.deleteById(id);
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

    private SuggestionRequestContext buildContext(String difficultyRaw, String classNameRaw, String tierRaw) {
        return new SuggestionRequestContext(
                normalizeClassName(classNameRaw),
                mapDifficulty(difficultyRaw),
                mapTier(tierRaw)
        );
    }

    private SuggestionRequestContext buildContext(String difficultyRaw, String classNameRaw, SuggestionTier tier) {
        return new SuggestionRequestContext(
                normalizeClassName(classNameRaw),
                mapDifficulty(difficultyRaw),
                tier
        );
    }

    private List<Suggestion> findSuggestions(SuggestionRequestContext context) {
        return suggestionRepository.findByDifficultyAndClassNameIgnoreCaseAndTier(
                context.difficulty(), context.className(), context.tier());
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

    private Set<Long> loadDeliveredSuggestions(String sessionKey) {
        return deliveredSuggestionRepository.findBySessionKey(sessionKey)
                .stream()
                .sorted(java.util.Comparator.comparing(DeliveredSuggestion::getCreatedAt))
                .map(DeliveredSuggestion::getSuggestionId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Set<Long> purgeInvalidDelivered(String sessionKey, Set<Long> delivered, Set<Long> validIds) {
        Set<Long> invalid = delivered.stream()
                .filter(id -> !validIds.contains(id))
                .collect(Collectors.toSet());
        if (!invalid.isEmpty()) {
            deliveredSuggestionRepository.deleteBySessionKeyAndSuggestionIdIn(sessionKey, invalid);
            delivered.removeAll(invalid);
        }
        return delivered;
    }

    private void persistDeliveredSuggestion(String sessionKey, Long suggestionId) {
        DeliveredSuggestion entity = DeliveredSuggestion.builder()
                .sessionKey(sessionKey)
                .suggestionId(suggestionId)
                .build();
        deliveredSuggestionRepository.save(entity);
    }

    private void clearDeliveredSession(String sessionKey) {
        deliveredSuggestionRepository.deleteBySessionKey(sessionKey);
    }

    private void maybeResetSession(Integer remainingClient, int effectiveCap, String sessionKey, Long gameId) {
        // Se la partita ha un identificativo valido, usiamo sempre lo stato server-side senza fidarci del contatore client.
        if (gameId != null && gameId > 0) {
            return;
        }
        if (remainingClient != null && remainingClient >= effectiveCap) {
            clearDeliveredSession(sessionKey);
        }
    }

    private int capForTier(SuggestionDifficulty difficulty, SuggestionTier tier, int availableCount) {
        if (tier == SuggestionTier.ADVANCED) {
            return availableCount;
        }
        return Math.min(maxForDifficulty(difficulty), availableCount);
    }

    private int maxForDifficulty(SuggestionDifficulty difficulty) {
        // Numero massimo teorico per difficolta; l'effettivo viene poi limitato dal numero reale di suggerimenti presenti.
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

    private SuggestionAvailabilityResponseDTO availabilityResponse(int availableCount, int suggestionsMax, List<String> delivered) {
        return SuggestionAvailabilityResponseDTO.builder()
                .availableSuggestions(availableCount)
                .suggestionsMax(suggestionsMax)
                .totalAvailableSuggestions(suggestionsMax)
                .deliveredSuggestions(delivered)
                .build();
    }

    private int determineAdvancedCost(Integer requestedCost) {
        return (requestedCost != null && requestedCost > 0) ? requestedCost : DEFAULT_ADVANCED_COST;
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

    private static final class SuggestionRequestContext {
        private final String className;
        private final SuggestionDifficulty difficulty;
        private final SuggestionTier tier;

        private SuggestionRequestContext(String className, SuggestionDifficulty difficulty, SuggestionTier tier) {
            this.className = className;
            this.difficulty = difficulty;
            this.tier = tier;
        }

        private String className() {
            return className;
        }

        private SuggestionDifficulty difficulty() {
            return difficulty;
        }

        private SuggestionTier tier() {
            return tier;
        }

        private boolean isAdvanced() {
            return SuggestionTier.ADVANCED.equals(tier);
        }
    }
}
