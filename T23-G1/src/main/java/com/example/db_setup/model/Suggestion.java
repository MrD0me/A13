package com.example.db_setup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.Instant;

/**
 * Entità che rappresenta un suggerimento mostrabile al giocatore durante una partita.
 * I suggerimenti vengono persistiti nel database così da poter essere aggiornati
 * dinamicamente senza dover ricompilare l'applicazione.
 */
@Entity
@Table(name = "suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Testo del suggerimento da mostrare all'utente.
     */
    @Column(nullable = false, length = 1024)
    private String text;

    /**
     * Nome completo della classe a cui il suggerimento si riferisce.
     */
    @Column(name = "class_name", length = 255)
    private String className;

    /**
     * Difficoltà a cui appartiene il suggerimento (EASY, MEDIUM, HARD).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private SuggestionDifficulty difficulty;

    /**
     * Lingua del suggerimento, utile per il supporto multi lingua futuro.
     */
    @Column(length = 8)
    private String language;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
