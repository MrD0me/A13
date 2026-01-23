package com.groom.manvsclass.model.dto.suggestion;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO che incapsula le informazioni necessarie per richiedere uno o piu suggerimenti
 * dal servizio backend.
 */
@Data
public class SuggestionRequestDTO {
    private Long gameId;

    @NotBlank
    private String difficulty;

    @NotNull
    @Min(0)
    private Integer remainingSuggestions;

    @NotBlank
    private String className;
}
