package com.groom.manvsclass.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "interaction")
public class interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_i; //Id interazione

    private String email; //Email utente

    @Column(name = "class_name")
    private String name; //Id classe

    @Column(name = "user_id")
    private long id; //Id utente

    private int type; //Tipo di interazione
    private String commento;
    private String date;

    public interaction() {
    }

    public interaction(Long id_i, String email, String name, long id, int type, String date) {
        this.id_i = id_i;
        this.email = email;
        this.name = name;
        this.id = id;
        this.type = type;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "interaction{" +
                "class_id= " + id_i + '\'' +
                "class_name='" + name + '\'' +
                "user_id='" + id + '\'' +
                "user_email=" + email + '\'' +
                ", type='" + type + '\'' +
                ", commento='" + commento +
                "date=" + date + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId_i() {
        return id_i;
    }

    public void setId_i(Long id_i) {
        this.id_i = id_i;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
