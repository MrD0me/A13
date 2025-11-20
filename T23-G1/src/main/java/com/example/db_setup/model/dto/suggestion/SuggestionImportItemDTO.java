package com.example.db_setup.model.dto.suggestion;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SuggestionImportItemDTO {

    @NotBlank
    private String difficulty;

    @NotBlank
    private String text;
}
