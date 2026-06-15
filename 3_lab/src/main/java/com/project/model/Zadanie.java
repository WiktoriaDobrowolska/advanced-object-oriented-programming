package com.project.model;

import java.time.LocalDate;

public class Zadanie {
    private Integer zadanieId;
    private Integer projektId;
    private String nazwa;
    private String opis;
    private LocalDate data;

    public Zadanie() {
    }

    public Zadanie(Integer zadanieId, Integer projektId, String nazwa, String opis, LocalDate data) {
        this.zadanieId = zadanieId;
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.data = data;
    }

    public Integer getZadanieId() { return zadanieId; }
    public void setZadanieId(Integer zadanieId) { this.zadanieId = zadanieId; }

    public Integer getProjektId() { return projektId; }
    public void setProjektId(Integer projektId) { this.projektId = projektId; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
}
