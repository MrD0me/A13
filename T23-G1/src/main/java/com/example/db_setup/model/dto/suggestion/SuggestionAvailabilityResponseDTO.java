package com.example.db_setup.model.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risposta che espone il numero di suggerimenti distinti realmente disponibili per classe/difficoltà.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionAvailabilityResponseDTO {
    private int availableSuggestions;
    private int suggestionsMax;
    private int totalAvailableSuggestions;
}
