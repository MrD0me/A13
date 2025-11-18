package com.groom.manvsclass.model.dto;

/**
 * DTO per la richiesta di suggerimenti con informazioni sulla partita
 */
public class SuggestionRequestDTO {
    private long gameId;
    private String difficulty;
    private int remainingSuggestions;

    public SuggestionRequestDTO() {
    }

    public SuggestionRequestDTO(long gameId, String difficulty, int remainingSuggestions) {
        this.gameId = gameId;
        this.difficulty = difficulty;
        this.remainingSuggestions = remainingSuggestions;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getRemainingSuggestions() {
        return remainingSuggestions;
    }

    public void setRemainingSuggestions(int remainingSuggestions) {
        this.remainingSuggestions = remainingSuggestions;
    }
}
