package com.groom.manvsclass.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @Column(length = 64)
    private String id;

    private String text;
    private String category;
    private String difficulty; // EASY, MEDIUM, HARD

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public Suggestion() {
    }

    public Suggestion(String text, String category) {
        this.text = text;
        this.category = category;
    }

    public Suggestion(String text, String category, String difficulty) {
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
