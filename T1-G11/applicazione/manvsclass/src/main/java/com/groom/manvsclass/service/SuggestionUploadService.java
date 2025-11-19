package com.groom.manvsclass.service;

import com.groom.manvsclass.model.Suggestion;
import com.groom.manvsclass.model.dto.SuggestionImportClientRequest;
import com.groom.manvsclass.model.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionUploadService {

    private static final Set<String> ALLOWED_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    private final SuggestionRepository suggestionRepository;
    private final RestTemplate restTemplate;

    @Value("${suggestions.t23.base-url:http://t23-controller:8082}")
    private String suggestionServiceBaseUrl;

    public void uploadSuggestionsFromFile(String className, MultipartFile file) {
        String normalizedClassName = normalizeClassName(className);
        List<SuggestionUploadItem> parsedSuggestions = parseFile(file);

        suggestionRepository.deleteByClassNameIgnoreCase(normalizedClassName);
        List<Suggestion> entities = parsedSuggestions.stream()
                .map(item -> {
                    Suggestion suggestion = new Suggestion();
                    suggestion.setText(item.text());
                    suggestion.setDifficulty(item.difficulty());
                    suggestion.setClassName(normalizedClassName);
                    return suggestion;
                })
                .collect(Collectors.toList());
        suggestionRepository.saveAll(entities);

        sendPayloadToT23(normalizedClassName, parsedSuggestions);
    }

    private void sendPayloadToT23(String className, List<SuggestionUploadItem> items) {
        SuggestionImportClientRequest request = new SuggestionImportClientRequest();
        request.setClassName(className);
        List<SuggestionImportClientRequest.Item> dtoItems = items.stream()
                .map(item -> {
                    SuggestionImportClientRequest.Item dto = new SuggestionImportClientRequest.Item();
                    dto.setDifficulty(item.difficulty());
                    dto.setText(item.text());
                    return dto;
                })
                .collect(Collectors.toList());
        request.setSuggestions(dtoItems);

        String url = suggestionServiceBaseUrl + "/suggerimenti/import";
        log.info("Invio {} suggerimenti a {}", dtoItems.size(), url);
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (RuntimeException ex) {
            log.error("Errore durante l'invio dei suggerimenti a {}", url, ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Impossibile sincronizzare i suggerimenti con il servizio utente", ex);
        }
    }

    private List<SuggestionUploadItem> parseFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il file dei suggerimenti e vuoto");
        }

        List<SuggestionUploadItem> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";", 2);
                if (parts.length != 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Formato non valido alla riga " + lineNumber + ". Usa la sintassi DIFFICOLTA;testo");
                }
                String difficulty = normalizeDifficulty(parts[0]);
                String text = normalizeText(parts[1]);
                items.add(new SuggestionUploadItem(difficulty, text));
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nella lettura del file dei suggerimenti", e);
        }

        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il file non contiene suggerimenti validi");
        }
        return items;
    }

    private String normalizeClassName(String className) {
        if (!StringUtils.hasText(className)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il nome della classe e obbligatorio");
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

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specificare la difficolta in ogni riga");
        }
        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_DIFFICULTIES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Difficolta non valida: " + normalized + ". Valori ammessi " + ALLOWED_DIFFICULTIES);
        }
        return normalized;
    }

    private record SuggestionUploadItem(String difficulty, String text) {
    }
}
