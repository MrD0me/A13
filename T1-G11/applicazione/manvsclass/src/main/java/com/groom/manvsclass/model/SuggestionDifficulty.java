package com.groom.manvsclass.model;

/**
 * Tipi di difficoltتْ ammessi per i suggerimenti mostrati all'utente.
 */
public enum SuggestionDifficulty {
    EASY,
    MEDIUM,
    HARD;

    public static SuggestionDifficulty fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("La difficolta e obbligatoria");
        }
        try {
            return SuggestionDifficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valore difficolta non valido: " + value);
        }
    }
}
