package com.project.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="zadanie")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Zadanie {

    @ManyToOne
    @JoinColumn(name = "projekt_id")
    @JsonIgnoreProperties("zadania")
    private Projekt projekt;

    @Id
    @GeneratedValue
    @Column(name="zadanie_id")
    private Integer zadanieId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column()
    private Integer kolejnosc;

    @Column(length = 1000)
    private String opis;

    @CreatedDate
    @Column(name = "dataczas_dodania", updatable = false, nullable = false)
    private LocalDateTime dataczasDodania;
}

/* TODO:
 * Uzupełnij kod o zmienne reprezentujące pozostałe pola tabeli zadanie (patrz rys. 3.1),
 * następnie wygeneruj dla nich tzw. akcesory i mutatory (Source -> Generate Getters and Setters).
 * Ponadto dodaj pusty konstruktor oraz konstruktor uwzględniający wszystkie pola.
 * Alternatywnie wykorzystaj adnotacje biblioteki Lombok.
 */