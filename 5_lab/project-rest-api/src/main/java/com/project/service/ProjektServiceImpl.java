package com.project.service;

import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjektServiceImpl implements ProjektService {
    private final ProjektRepository projektRepository;
    private final ZadanieRepository zadanieRepository;

    @Autowired
    public ProjektServiceImpl(ProjektRepository projektRepository, ZadanieRepository zadanieRepository) {
        this.projektRepository = projektRepository;
        this.zadanieRepository = zadanieRepository;
    }

    @Override
    public Optional<Projekt> getProjekt(Integer projektId) {
        return projektRepository.findById(projektId);
    }

    @Override
    @Transactional
    public Projekt setProjekt(Projekt projekt) {
        if (projekt.getProjektId() != null) {
            Projekt zapisanyProjekt = projektRepository.findById(projekt.getProjektId()).orElseThrow();
            zapisanyProjekt.setNazwa(projekt.getNazwa());
            zapisanyProjekt.setOpis(projekt.getOpis());
            zapisanyProjekt.setDataOddania(projekt.getDataOddania());
            return projektRepository.save(zapisanyProjekt);
        }
        return projektRepository.save(projekt);
    }

    @Override
    @Transactional
    public void deleteProjekt(Integer projektId) {
        Projekt projekt = projektRepository.findById(projektId).orElseThrow();

        // Relacja projekt-student jest po stronie Projekt, więc przed usunięciem projektu czyścimy tabelę łączącą.
        if (projekt.getStudenci() != null) {
            projekt.getStudenci().clear();
            projektRepository.save(projekt);
        }

        for (Zadanie zadanie : zadanieRepository.findZadaniaProjektu(projektId)) {
            zadanieRepository.delete(zadanie);
        }
        projektRepository.delete(projekt);
    }

    @Override
    public Page<Projekt> getProjekty(Pageable pageable) {
        return projektRepository.findAll(pageable);
    }

    @Override
    public Page<Projekt> searchByNazwa(String nazwa, Pageable pageable) {
        return projektRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
    }
}
