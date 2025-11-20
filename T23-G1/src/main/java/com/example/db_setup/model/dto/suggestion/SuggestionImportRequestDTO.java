package com.example.db_setup.model.dto.suggestion;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SuggestionImportRequestDTO {

    @NotBlank
    private String className;

    @Valid
    @NotEmpty
    private List<SuggestionImportItemDTO> suggestions;
}
