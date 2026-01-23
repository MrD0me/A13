package com.groom.manvsclass.model.dto.suggestion;

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
    /** Alias esplicito per il denominatore del contatore, cosi il client non deve calcolarlo. */
    private int suggestionsMax;
    /** Numero totale di suggerimenti distinti effettivamente disponibili per questa richiesta. */
    private int totalAvailableSuggestions;
    /** Alias per il numeratore da mostrare (compatibilita con eventuali key esistenti lato UI). */
    private int suggestionsAvailable;
    private boolean noMoreSuggestions;
    private String message;

    /** Crediti residui dopo l'eventuale acquisto di un suggerimento avanzato. */
    private Integer creditsLeft;
    /** Crediti consumati per ottenere il suggerimento. */
    private Integer creditsSpent;
    /** Costo per singolo suggerimento. */
    private Integer suggestionCost;
    /** Tier del suggerimento servito (BASE o ADVANCED). */
    private String tier;
}
