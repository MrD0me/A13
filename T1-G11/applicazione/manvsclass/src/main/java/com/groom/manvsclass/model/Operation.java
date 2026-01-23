package com.groom.manvsclass.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_op;

    @Column(nullable = false)
    private String username_admin;

    @Column(nullable = false)
    private String nome_classe;

    private int type;
    private String date;

    public Operation() {
    }

    public Operation(Long id_op, String username_admin, String nome_classe, int type, String date) {
        this.id_op = id_op;
        this.username_admin = username_admin;
        this.nome_classe = nome_classe;
        this.type = type;
        this.date = date;
    }

    public Long getId_op() {
        return id_op;
    }

    public void setId_op(Long id_op) {
        this.id_op = id_op;
    }

    public String getUsername_admin() {
        return username_admin;
    }

    public void setUsername_admin(String username_admin) {
        this.username_admin = username_admin;
    }

    public String getNome_classe() {
        return nome_classe;
    }

    public void setNome_classe(String nome_classe) {
        this.nome_classe = nome_classe;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id_op=" + id_op +
                ", username_admin='" + username_admin + '\'' +
                ", nome_classe='" + nome_classe + '\'' +
                ", type=" + type +
                ", date='" + date + '\'' +
                '}';
    }
}
