package com.groom.manvsclass.model.dto;

import java.util.List;

/**
 * DTO per la risposta ai suggerimenti con il contatore
 */
public class SuggestionResponseDTO {
    private List<String> suggestions;
    private int remainingSuggestions;
    private boolean noMoreSuggestions;
    private String message;

    public SuggestionResponseDTO() {
    }

    public SuggestionResponseDTO(List<String> suggestions, int remainingSuggestions, boolean noMoreSuggestions, String message) {
        this.suggestions = suggestions;
        this.remainingSuggestions = remainingSuggestions;
        this.noMoreSuggestions = noMoreSuggestions;
        this.message = message;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public int getRemainingSuggestions() {
        return remainingSuggestions;
    }

    public void setRemainingSuggestions(int remainingSuggestions) {
        this.remainingSuggestions = remainingSuggestions;
    }

    public boolean isNoMoreSuggestions() {
        return noMoreSuggestions;
    }

    public void setNoMoreSuggestions(boolean noMoreSuggestions) {
        this.noMoreSuggestions = noMoreSuggestions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
