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
    private int remainingSuggestions;
    private boolean noMoreSuggestions;
    private String message;
}
