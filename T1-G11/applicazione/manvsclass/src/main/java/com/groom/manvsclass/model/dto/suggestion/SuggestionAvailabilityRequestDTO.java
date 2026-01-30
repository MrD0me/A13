package com.groom.manvsclass.model.dto.suggestion;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Richiesta per ottenere solo la disponibilita dei suggerimenti senza consumarne uno.
 */
@Data
public class SuggestionAvailabilityRequestDTO {

    @NotBlank
    private String difficulty;

    @NotBlank
    private String className;

    /**
     * Facoltativo: BASE (default) oppure ADVANCED.
     */
    private String tier;

    /**
     * Identificativo partita usato per ricostruire lo stato server-side
     * (suggerimenti già consegnati) anche cambiando browser/dispositivo.
     */
    private Long gameId;
}
