package com.project.dao;

import java.time.LocalDate;
import java.util.List;
import com.project.model.Projekt;

public interface ProjektDAO {

    List<Projekt> getProjekty(String search, Integer offset, Integer limit);
    void setProjekt(Projekt projekt);
    void deleteProjekt(Integer projektId);
    int getRowsNumber();

    List<Projekt> getProjekty(Integer offset, Integer limit);
    List<Projekt> getProjektyWhereNazwaLike(String nazwa, Integer offset, Integer limit);
    List<Projekt> getProjektyWhereDataOddaniaIs(LocalDate dataOddania, Integer offset, Integer limit);
    int getRowsNumberWhereNazwaLike(String nazwa);
    int getRowsNumberWhereDataOddaniaIs(LocalDate dataOddania);
}