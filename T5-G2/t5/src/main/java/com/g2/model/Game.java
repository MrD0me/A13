package com.g2.model;

import java.time.LocalDate;

public class Game {
    private int playerId;
    private long id;
    private String description;
    private String name;
    private String difficulty;
    private LocalDate data_creazione;
    private String ora_creazione;
    private String classe;
    private String username;
    private double score;
    private int suggestionsAvailable; // Numero di suggerimenti disponibili per la partita

    public Game(int playerId, String description, String name, String difficulty, String username) {
        this.playerId = playerId;
        this.description = description;
        this.name = name;
        this.difficulty = difficulty;
        this.classe = "";
        this.username = username;
        this.suggestionsAvailable = initSuggestionsCount(difficulty); // Inizializza in base alla difficoltà
    }

    public Game() {

    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDate getData_creazione() {
        return data_creazione;
    }

    public void setData_creazione(LocalDate data_creazione) {
        this.data_creazione = data_creazione;
    }

    public String getOra_creazione() {
        return ora_creazione;
    }

    public void setOra_creazione(String ora_creazione) {
        this.ora_creazione = ora_creazione;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getSuggestionsAvailable() {
        return suggestionsAvailable;
    }

    public void setSuggestionsAvailable(int suggestionsAvailable) {
        this.suggestionsAvailable = suggestionsAvailable;
    }

    /**
     * Decrementa il numero di suggerimenti disponibili
     * @return il numero di suggerimenti rimasti dopo il decremento
     */
    public int decrementSuggestions() {
        if (suggestionsAvailable > 0) {
            suggestionsAvailable--;
        }
        return suggestionsAvailable;
    }

    /**
     * Inizializza il numero di suggerimenti in base alla difficoltà
     * - HARD: 0 suggerimenti
     * - MEDIUM: 3 suggerimenti
     * - EASY: 5 suggerimenti
     * @param difficulty la difficoltà della partita
     * @return il numero di suggerimenti
     */
    private int initSuggestionsCount(String difficulty) {
        if (difficulty == null) return 0;

        switch (difficulty.toUpperCase()) {
            case "HARD":
                return 0;
            case "MEDIUM":
                return 3;
            case "EASY":
                return 5;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return "Game{" +
                "playerId=" + playerId +
                ", id=" + id +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", data_creazione=" + data_creazione +
                ", ora_creazione='" + ora_creazione + '\'' +
                ", classe='" + classe + '\'' +
                ", username='" + username + '\'' +
                ", suggestionsAvailable=" + suggestionsAvailable +
                '}';
    }
}
