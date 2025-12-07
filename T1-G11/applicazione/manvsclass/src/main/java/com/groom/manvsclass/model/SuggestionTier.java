package com.groom.manvsclass.model;

/**
 * Identifica il tipo di suggerimento.
 * <p>
 * I suggerimenti base sono gratuiti, mentre quelli avanzati richiedono crediti.
 * </p>
 */
public enum SuggestionTier {
    BASE,
    ADVANCED;

    public static SuggestionTier fromString(String value) {
        if (value == null || value.isBlank()) {
            return BASE;
        }
        try {
            return SuggestionTier.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valore tier non valido: " + value);
        }
    }
}
