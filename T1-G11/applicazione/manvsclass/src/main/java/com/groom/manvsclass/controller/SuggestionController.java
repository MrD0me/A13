package com.groom.manvsclass.controller;

import com.groom.manvsclass.model.dto.SuggestionRequestDTO;
import com.groom.manvsclass.model.dto.SuggestionResponseDTO;
import com.groom.manvsclass.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    /**
     * Recupera tutti i suggerimenti dal database
     * @param code il codice dell'utente (parametro non utilizzato ma mantenuto per compatibilità)
     * @return lista di suggerimenti
     */
    @GetMapping("/suggerimenti")
    public ResponseEntity<List<String>> getSuggerimenti(@RequestBody String code) {
        List<String> suggerimenti = suggestionService.getAllSuggestions();
        return ResponseEntity.ok(suggerimenti);
    }

    /**
     * Recupera i suggerimenti per categoria
     * @param category la categoria dei suggerimenti
     * @return lista di suggerimenti per categoria
     */
    @GetMapping("/suggerimenti/categoria/{category}")
    public ResponseEntity<List<String>> getSuggestionsByCategory(@PathVariable String category) {
        List<String> suggerimenti = suggestionService.getSuggestionsByCategory(category);
        return ResponseEntity.ok(suggerimenti);
    }

    /**
     * Endpoint per richiedere un suggerimento durante una partita
     * Decrementa il numero di suggerimenti disponibili e ritorna i suggerimenti con il contatore aggiornato
     * @param request DTO con gameId, difficulty e remainingSuggestions
     * @return SuggestionResponseDTO con suggerimenti, numero rimasto e messaggio di avviso se finiti
     */
    @PostMapping("/suggerimenti/richiedi")
    public ResponseEntity<SuggestionResponseDTO> requestSuggestion(@RequestBody SuggestionRequestDTO request) {
        // Se i suggerimenti sono già a 0, ritorna un messaggio di avviso
        if (request.getRemainingSuggestions() <= 0) {
            SuggestionResponseDTO response = new SuggestionResponseDTO(
                    null,
                    0,
                    true,
                    "Non hai più suggerimenti disponibili per questa partita!"
            );
            return ResponseEntity.ok(response);
        }

        // Decrementa i suggerimenti
        int remainingSuggestions = request.getRemainingSuggestions() - 1;

        // Recupera i suggerimenti per difficoltà e sceglie uno a caso per mostrare un solo suggerimento per click
        List<String> suggestions = suggestionService.getSuggestionsByDifficulty(request.getDifficulty());
        List<String> chosen = Collections.emptyList();
        if (suggestions != null && !suggestions.isEmpty()) {
            Random random = new Random();
            String single = suggestions.get(random.nextInt(suggestions.size()));
            chosen = Collections.singletonList(single);
        }

        // Se è l'ultimo suggerimento, prepara il messaggio di avviso
        String message = "";
        boolean noMore = false;
        if (remainingSuggestions == 0) {
            message = "Questo era il tuo ultimo suggerimento!";
            noMore = true;
        }

        SuggestionResponseDTO response = new SuggestionResponseDTO(
                chosen,
                remainingSuggestions,
                noMore,
                message
        );

        return ResponseEntity.ok(response);
    }
}
