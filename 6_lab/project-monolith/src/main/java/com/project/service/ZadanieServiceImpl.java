package com.project.service;

import com.project.model.Zadanie;
import com.project.repository.StudentRepository;
import com.project.repository.ZadanieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ZadanieServiceImpl implements ZadanieService{
    private final ZadanieRepository zadanieRepository;

    @Autowired
    public ZadanieServiceImpl(ZadanieRepository zadanieRepository) {
        this.zadanieRepository = zadanieRepository;
    }

    @Override
    public Optional<Zadanie> getZadanie(Integer ZadanieId) {
        return zadanieRepository.findById(ZadanieId);
    }

    @Override
    public Zadanie setZadanie(Zadanie Zadanie) {
        return zadanieRepository.save(Zadanie);
    }

    @Override
    public void deleteZadanie(Integer ZadanieId) {
        zadanieRepository.deleteById(ZadanieId);
    }

    @Override
    public Page<Zadanie> getZadania(Pageable pageable) {
        return zadanieRepository.findAll(pageable);
    }

    @Override
    public Page<Zadanie> searchByNazwa(String nazwa, Pageable pageable) {
        return zadanieRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
    }

    @Override
    public Page<Zadanie> getZadaniaProjektu(Integer projektId, Pageable pageable) {
        return zadanieRepository.findZadaniaProjektu(projektId, pageable);
    }
}
