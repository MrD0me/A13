package com.groom.manvsclass.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "class_ut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassUT {

    @Id
    private String name;
    private String date;
    private String difficulty;
    private String uri;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> category;

    @Override
    public String toString() {
        return "ClassUT{" +
                "name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", code_url='" + uri + '\'' +
                ", category=" + category +
                '}';
    }

}
