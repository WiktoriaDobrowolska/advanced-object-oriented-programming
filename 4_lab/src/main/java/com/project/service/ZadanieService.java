package com.project.service;

import com.project.model.Zadanie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ZadanieService {
    Optional<Zadanie> getZadanie(Integer ZadanieId);
    Zadanie setZadanie(Zadanie Zadanie);
    void deleteZadanie(Integer ZadanieId);
    Page<Zadanie> getZadania(Pageable pageable);
    Page<Zadanie> searchByNazwa(String nazwa, Pageable pageable);
    Page<Zadanie> getZadaniaProjektu(Integer projektId, Pageable pageable);
}
