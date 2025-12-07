package com.groom.manvsclass.model;

import com.groom.manvsclass.model.converter.EvosuiteScoreConverter;
import com.groom.manvsclass.model.converter.JacocoScoreConverter;
import org.hibernate.annotations.CreationTimestamp;
import testrobotchallenge.commons.models.opponent.OpponentDifficulty;
import testrobotchallenge.commons.models.score.EvosuiteScore;
import testrobotchallenge.commons.models.score.JacocoScore;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "opponents")
public class Opponent {

    @Id
    @Column(length = 64)
    private String id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String classUT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpponentDifficulty opponentDifficulty;

    @Column(nullable = false)
    private String opponentType;

    @Lob
    private String coverage;

    @Convert(converter = JacocoScoreConverter.class)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private JacocoScore jacocoScore;

    @Convert(converter = EvosuiteScoreConverter.class)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private EvosuiteScore evosuiteScore;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getClassUT() {
        return classUT;
    }

    public void setClassUT(String classUT) {
        this.classUT = classUT;
    }

    public OpponentDifficulty getOpponentDifficulty() {
        return opponentDifficulty;
    }

    public void setOpponentDifficulty(OpponentDifficulty opponentDifficulty) {
        this.opponentDifficulty = opponentDifficulty;
    }

    public String getOpponentType() {
        return opponentType;
    }

    public void setOpponentType(String opponentType) {
        this.opponentType = opponentType;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public JacocoScore getJacocoScore() {
        return jacocoScore;
    }

    public void setJacocoScore(JacocoScore jacocoScore) {
        this.jacocoScore = jacocoScore;
    }

    public EvosuiteScore getEvosuiteScore() {
        return evosuiteScore;
    }

    public void setEvosuiteScore(EvosuiteScore evosuiteScore) {
        this.evosuiteScore = evosuiteScore;
    }

    @Override
    public String toString() {
        return "Opponent{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", classUT='" + classUT + '\'' +
                ", opponentDifficulty=" + opponentDifficulty +
                ", opponentType='" + opponentType + '\'' +
                ", coverage='" + coverage + '\'' +
                ", jacocoScore=" + jacocoScore +
                ", evosuiteScore=" + evosuiteScore +
                '}';
    }
}
