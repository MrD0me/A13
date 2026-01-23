package com.groom.manvsclass.model.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionListItemDTO {
    private Long id;
    private String text;
    private String className;
    private String difficulty;
    private String tier;
    private String language;
}
