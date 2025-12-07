package com.groom.manvsclass.model.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
