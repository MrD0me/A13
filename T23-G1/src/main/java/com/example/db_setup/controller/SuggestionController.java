package com.example.db_setup.controller;

import com.example.db_setup.model.dto.suggestion.SuggestionImportRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionRequestDTO;
import com.example.db_setup.model.dto.suggestion.SuggestionResponseDTO;
import com.example.db_setup.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping("/suggerimenti")
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

    private final SuggestionService suggestionService;

    @Operation(
            summary = "Recupera un suggerimento dal database",
            description = "Restituisce un suggerimento coerente con la difficoltà della partita"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggerimento restituito correttamente"),
            @ApiResponse(responseCode = "404", description = "Nessun suggerimento disponibile")
    })
    @PostMapping("/richiedi")
    public ResponseEntity<SuggestionResponseDTO> requestSuggestion(@Valid @RequestBody SuggestionRequestDTO request) {
        log.info("[POST /suggerimenti/richiedi] request: {}", request);
        SuggestionResponseDTO response = suggestionService.requestSuggestions(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Importa i suggerimenti per una classe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Suggerimenti salvati correttamente"),
            @ApiResponse(responseCode = "400", description = "Formato file non valido")
    })
    @PostMapping("/import")
    public ResponseEntity<Void> importSuggestions(@Valid @RequestBody SuggestionImportRequestDTO request) {
        log.info("[POST /suggerimenti/import] className={}", request.getClassName());
        suggestionService.replaceSuggestions(request);
        return ResponseEntity.noContent().build();
    }
}
