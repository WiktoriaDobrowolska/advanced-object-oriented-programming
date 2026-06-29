package com.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class Projekt { //np.@Table(name="projekt", indexes = @Index(name = "idx_nazwa_projektu", columnList = "nazwa"))

    private Integer projektId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    private String nazwa;

    @JsonIgnore
    private List<Zadanie> zadania;

    @JsonIgnore
    private Set<Student> studenci;

    private String opis;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataczasUtworzenia;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataczasModyfikacji;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataOddania;
}

