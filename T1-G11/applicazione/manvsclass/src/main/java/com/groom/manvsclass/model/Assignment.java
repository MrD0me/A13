package com.groom.manvsclass.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @Column(length = 64)
    private String idAssignment;

    @Column(length = 64)
    private String teamId;

    private String nomeTeam;

    @Column(nullable = false)
    private String titolo;

    private String descrizione;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCreazione;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataScadenza;

    public Assignment() {
        this.dataCreazione = new Date();
    }

    public Assignment(String titolo, String descrizione, Date dataScadenza) {
        this.idAssignment = null;
        this.teamId = null;
        this.nomeTeam = null;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.dataCreazione = new Date();
        this.dataScadenza = dataScadenza;
    }

    public String getIdAssignment() {
        return idAssignment;
    }

    public void setIdAssignment(String idAssignment) {
        this.idAssignment = idAssignment;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public Date getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(Date dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setIdTeam(String idTeam) {
        this.teamId = idTeam;
    }

    public String getNomeTeam() {
        return nomeTeam;
    }

    public void setNomeTeam(String nomeTeam) {
        this.nomeTeam = nomeTeam;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "idAssignment='" + idAssignment + '\'' +
                ", idTeam='" + teamId + '\'' +
                ", nomeTeam='" + nomeTeam + '\'' +
                ", titolo='" + titolo + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", dataCreazione=" + dataCreazione +
                ", dataScadenza=" + dataScadenza +
                '}';
    }
}
