package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;



@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class Student {

    @JsonIgnoreProperties("studenci")
    private Set<Projekt> projekty;

    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(min = 2, max = 50, message = "Imię musi zawierać od {min} do {max} znaków!")
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(min = 2, max = 100, message = "Nazwisko musi zawierać od {min} do {max} znaków!")
    private String nazwisko;

    @NotBlank(message = "Numer indeksu nie może być pusty!")
    @Size(min = 3, max = 20, message = "Numer indeksu musi zawierać od {min} do {max} znaków!")
    private String nrIndeksu;

    @NotBlank(message = "Email nie może być pusty!")
    @Size(min = 5, max = 50, message = "Email musi zawierać od {min} do {max} znaków!")
    private String email;

    private Boolean stacjonarny;
}