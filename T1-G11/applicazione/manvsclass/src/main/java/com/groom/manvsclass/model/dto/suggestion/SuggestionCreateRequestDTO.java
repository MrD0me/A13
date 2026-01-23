package com.groom.manvsclass.model.dto.suggestion;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SuggestionCreateRequestDTO {
    @NotBlank
    private String className;

    @NotBlank
    private String difficulty;

    @NotBlank
    private String text;

    /**
     * Facoltativo: BASE (default) oppure ADVANCED.
     */
    private String tier;

    /**
     * Facoltativo, default "it".
     */
    private String language;
}
