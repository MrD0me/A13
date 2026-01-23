package com.groom.manvsclass.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    private String name;
    private String description;
    private String statistic;
    private float progressRequired;

    public Achievement() {

    }

    public Achievement(Long ID, String name, String description, String statistic, float progressRequired) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.statistic = statistic;
        this.progressRequired = progressRequired;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatistic() {
        return statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public float getProgressRequired() {
        return progressRequired;
    }

    public void setProgressRequired(float progressRequired) {
        this.progressRequired = progressRequired;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", statistic=" + statistic +
                ", progressRequired=" + progressRequired +
                '}';
    }
}
