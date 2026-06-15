package com.project.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Set;

@Entity //Indeksujemy kolumny, które są najczęściej wykorzystywane do wyszukiwania studentów
@Table(name = "student",
        indexes = { @Index(name = "idx_nazwisko", columnList = "nazwisko", unique = false),
                @Index(name = "idx_nr_indeksu", columnList = "nr_indeksu", unique = true) })

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    /* TODO:
     * Uzupełnij kod o zmienne reprezentujące pozostałe pola tabeli 'projekt' (patrz rys. 3.1),
     * następnie wygeneruj dla nich tzw. akcesory i mutatory (Source -> Generate Getters and Setters).
     * Ponadto dodaj pusty konstruktor oraz konstruktor uwzględniający wszystkie pola.
     * Alternatywnie wykorzystaj adnotacje biblioteki Lombok.
     */
//...

    @ManyToMany(mappedBy = "studenci")
    @JsonIgnoreProperties("studenci")
    private Set<Projekt> projekty;

    @Id
    @GeneratedValue

    @Column(name = "student_id")
    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(min = 2, max = 50, message = "Imię musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 50)
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(min = 2, max = 100, message = "Nazwisko musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 100)
    private String nazwisko;

    @NotBlank(message = "Numer indeksu nie może być pusty!")
    @Size(min = 3, max = 20, message = "Numer indeksu musi zawierać od {min} do {max} znaków!")
    @Column(name = "nr_indeksu", unique = true, nullable = false, length = 20)
    private String nrIndeksu;

    @NotBlank(message = "Email nie może być pusty!")
    @Size(min = 5, max = 50, message = "Email musi zawierać od {min} do {max} znaków!")
    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean stacjonarny;
}