package com.example.db_setup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Tiene traccia dei suggerimenti già consegnati per una specifica sessione
 * (gameId|class|difficulty|tier). Serve a evitare duplicati anche dopo riavvio.
 */
@Entity
@Table(name = "delivered_suggestions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_key", "suggestion_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveredSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false, length = 255)
    private String sessionKey;

    @Column(name = "suggestion_id", nullable = false)
    private Long suggestionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
