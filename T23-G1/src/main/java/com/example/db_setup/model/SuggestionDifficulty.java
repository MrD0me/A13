package com.example.db_setup.model;

/**
 * Tipi di difficoltà ammessi per i suggerimenti mostrati all'utente.
 */
public enum SuggestionDifficulty {
    EASY,
    MEDIUM,
    HARD;

    public static SuggestionDifficulty fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("La difficoltà è obbligatoria");
        }
        try {
            return SuggestionDifficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valore difficoltà non valido: " + value);
        }
    }
}
