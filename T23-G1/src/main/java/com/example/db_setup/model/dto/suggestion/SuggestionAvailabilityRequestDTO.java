package com.example.db_setup.model.dto.suggestion;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Richiesta per ottenere solo la disponibilità dei suggerimenti senza consumarne uno.
 */
@Data
public class SuggestionAvailabilityRequestDTO {

    @NotBlank
    private String difficulty;

    @NotBlank
    private String className;
}
