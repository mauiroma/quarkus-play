package it.mauiroma.kafka;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Movie extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private int year;

    public Movie(){
    }


    public Movie(String title, int year){
        this.setTitle(title);
        this.setYear(year);
    }

    @Override
    public String toString() {
        return "{" +
                "\"title\":\""+getTitle()+"\"" +
                ",\"year\":"+getYear() +
                "}";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}