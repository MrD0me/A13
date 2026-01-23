package com.groom.manvsclass.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "scalate")
public class Scalata {

    @Id
    private String scalataName;

    private String username;
    private String scalataDescription;
    private int numberOfRounds;

    @ElementCollection
    private List<String> selectedClasses;

    public Scalata() {

    }

    public Scalata(String username, String scalataName, String scalataDescription, int numberOfRounds, List<String> selectedClasses) {
        this.username = username;
        this.scalataName = scalataName;
        this.scalataDescription = scalataDescription;
        this.numberOfRounds = numberOfRounds;
        this.selectedClasses = selectedClasses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScalataName() {
        return scalataName;
    }

    public void setScalataName(String scalataName) {
        this.scalataName = scalataName;
    }

    public String getScalataDescription() {
        return scalataDescription;
    }

    public void setScalataDescription(String scalataDescription) {
        this.scalataDescription = scalataDescription;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public List<String> getSelectedClasses() {
        return selectedClasses;
    }

    public void setSelectedClasses(List<String> selectedClasses) {
        this.selectedClasses = selectedClasses;
    }

    @Override
    public String toString() {
        return "Scalata [" +
                "author=" + username + ", " +
                "scalataName=" + scalataName + "," +
                "scalataDescription=" + scalataDescription + ", " +
                "rounds=" + numberOfRounds + ", " +
                "selectedClasses=" + selectedClasses +
                "]";
    }


}
