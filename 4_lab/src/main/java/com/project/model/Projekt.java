package com.project.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="projekt",
        indexes = @Index(name = "idx_nazwa_projektu", columnList = "nazwa"))
//TODO Indeksować kolumny, które są najczęściej wykorzystywane do wyszukiwania projektów
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Projekt { //np.@Table(name="projekt", indexes = @Index(name = "idx_nazwa_projektu", columnList = "nazwa"))

    @Id
    @GeneratedValue
    @Column(name="projekt_id") //tylko jeżeli nazwa kolumny w bazie danych ma być inna od nazwy zmiennej
    private Integer projektId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @OneToMany(mappedBy = "projekt")
    @JsonIgnoreProperties({"projekt"})
    private List<Zadanie> zadania;

    @ManyToMany
    @JoinTable(name = "projekt_student",
            joinColumns = {@JoinColumn(name="projekt_id")},
            inverseJoinColumns = {@JoinColumn(name="student_id")})
    @JsonIgnoreProperties({"projekty"})
    private Set<Student> studenci;

    @Column(length = 1000)
    private String opis;

    @CreatedDate
    @Column(name = "dataczas_utworzenia", nullable = false, updatable = false)
    private LocalDateTime dataczasUtworzenia;

    @Column(name = "data_oddania")
    private LocalDate dataOddania;
}

/* TODO:
 * Uzupełnij kod o zmienne reprezentujące pozostałe pola tabeli 'projekt' (patrz rys. 3.1),
 * następnie wygeneruj dla nich tzw. akcesory i mutatory (Source -> Generate Getters and Setters).
 * Ponadto dodaj pusty konstruktor oraz konstruktor uwzględniający wszystkie pola.
 * Alternatywnie wykorzystaj adnotacje biblioteki Lombok.
 */