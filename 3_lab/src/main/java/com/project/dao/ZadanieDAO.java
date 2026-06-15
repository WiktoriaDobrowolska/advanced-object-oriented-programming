package com.project.dao;

import com.project.model.Zadanie;
import java.util.List;

public interface ZadanieDAO {
    List<Zadanie> getZadania(Integer projektId);
    void setZadanie(Zadanie zadanie);
    void deleteZadanie(Integer zadanieId);
}
