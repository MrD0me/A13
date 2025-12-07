package com.groom.manvsclass.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "team_management")
public class TeamAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificativo univoco della relazione

    private String adminId; // Riferimento all'Admin (ID)
    private String teamId;  // Riferimento al Team (ID)
    private String teamName; //Nome della classe
    private String role; // Ruolo dell'Admin nel Team
    private boolean isActive; // Stato attuale della relazione

    public TeamAdmin() {
    }

    public TeamAdmin(String adminId, String teamId, String teamName, String role, boolean isActive) {
        this.adminId = adminId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.role = role;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return this.teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "TeamManagement{" +
                "id='" + id + '\'' +
                ", adminId='" + adminId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
