package com.groom.manvsclass.controller;

import com.groom.manvsclass.model.dto.suggestion.AdvancedSuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionAvailabilityResponseDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionCreateRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionImportRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionListItemDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionRequestDTO;
import com.groom.manvsclass.model.dto.suggestion.SuggestionResponseDTO;
import com.groom.manvsclass.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
            description = "Restituisce un suggerimento coerente con la difficolta della partita"
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

    @Operation(
            summary = "Recupera un suggerimento avanzato (a pagamento)",
            description = "Restituisce un suggerimento avanzato se il giocatore ha crediti disponibili"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggerimento avanzato restituito correttamente"),
            @ApiResponse(responseCode = "402", description = "Crediti insufficienti"),
            @ApiResponse(responseCode = "404", description = "Nessun suggerimento disponibile")
    })
    @PostMapping("/avanzati/richiedi")
    public ResponseEntity<SuggestionResponseDTO> requestAdvancedSuggestion(@Valid @RequestBody AdvancedSuggestionRequestDTO request) {
        log.info("[POST /suggerimenti/avanzati/richiedi] request: {}", request);
        SuggestionResponseDTO response = suggestionService.requestAdvancedSuggestions(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Recupera la disponibilita dei suggerimenti senza consumarli",
            description = "Ritorna quanti suggerimenti distinti sono realmente disponibili per classe e difficolta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilita calcolata (0 se nessun suggerimento disponibile)")
    })
    @PostMapping("/disponibilita")
    public ResponseEntity<SuggestionAvailabilityResponseDTO> getAvailability(@Valid @RequestBody SuggestionAvailabilityRequestDTO request) {
        log.info("[POST /suggerimenti/disponibilita] className={} difficulty={} tier={}", request.getClassName(), request.getDifficulty(), request.getTier());
        SuggestionAvailabilityResponseDTO response = suggestionService.getAvailability(request.getDifficulty(), request.getClassName(), request.getTier());
        return ResponseEntity.ok(response);
    }

    //Chiamata REST per consentire all'admin di importare i suggerimenti
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

    @Operation(
            summary = "Crea un nuovo suggerimento singolo",
            description = "Endpoint per uso admin: inserisce un nuovo suggerimento per classe/difficolta/tier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Suggerimento creato correttamente"),
            @ApiResponse(responseCode = "400", description = "Payload non valido")
    })
    @PostMapping("/admin/create")
    public ResponseEntity<Void> createSuggestion(@Valid @RequestBody SuggestionCreateRequestDTO request) {
        log.info("[POST /suggerimenti/admin/create] className={} difficulty={} tier={}", request.getClassName(), request.getDifficulty(), request.getTier());
        suggestionService.createSuggestion(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(
            summary = "Espone i limiti massimi di suggerimenti per difficolta",
            description = "Restituisce una mappa difficulty -> max suggerimenti per configurare il client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurazione restituita",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class)))
    })
    @PostMapping("/config")
    public ResponseEntity<java.util.Map<String, Integer>> getCaps() {
        log.info("[POST /suggerimenti/config] richiesta configurazione caps");
        return ResponseEntity.ok(suggestionService.getCaps());
    }

    @Operation(summary = "Lista suggerimenti per classe/difficolta (uso admin)")
    @GetMapping("/admin/list")
    public ResponseEntity<java.util.List<SuggestionListItemDTO>> listSuggestions(@RequestParam String className,
                                                                                 @RequestParam String difficulty,
                                                                                 @RequestParam(required = false) String tier) {
        log.info("[GET /suggerimenti/admin/list] className={} difficulty={} tier={}", className, difficulty, tier);
        return ResponseEntity.ok(suggestionService.listSuggestions(className, difficulty, tier));
    }

    @Operation(summary = "Elimina suggerimento (uso admin)")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteSuggestion(@PathVariable Long id) {
        log.info("[DELETE /suggerimenti/admin/{}]", id);
        suggestionService.deleteSuggestion(id);
        return ResponseEntity.noContent().build();
    }
}
