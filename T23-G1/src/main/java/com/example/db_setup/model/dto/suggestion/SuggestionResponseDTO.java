package com.example.db_setup.model.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO restituito al frontend quando vengono richiesti i suggerimenti.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponseDTO {

    private List<String> suggestions;
    /** Suggerimenti ancora erogabili dopo questa risposta. */
    private int remainingSuggestions;
    /** Alias esplicito per il denominatore del contatore, così il client non deve calcolarlo. */
    private int suggestionsMax;
    /** Numero totale di suggerimenti distinti effettivamente disponibili per questa richiesta. */
    private int totalAvailableSuggestions;
    /** Alias per il numeratore da mostrare (compatibilità con eventuali key esistenti lato UI). */
    private int suggestionsAvailable;
    private boolean noMoreSuggestions;
    private String message;
}
