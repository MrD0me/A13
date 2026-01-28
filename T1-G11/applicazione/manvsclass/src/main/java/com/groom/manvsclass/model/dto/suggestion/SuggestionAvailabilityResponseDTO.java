package com.groom.manvsclass.model.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Risposta che espone il numero di suggerimenti distinti realmente disponibili per classe/difficolta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionAvailabilityResponseDTO {
    private int availableSuggestions;
    private int suggestionsMax;
    private int totalAvailableSuggestions;
    /**
     * Lista dei suggerimenti già consegnati in questa partita (ordine non garantito).
     * Serve al client per ricostruire lo storico quando cambia dispositivo/browser.
     */
    private List<String> deliveredSuggestions;
}
