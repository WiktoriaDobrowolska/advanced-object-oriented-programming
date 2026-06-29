package com.project.service;

import com.project.model.Projekt;
import com.project.model.Student;
import com.project.repository.ProjektRepository;
import com.project.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final ProjektRepository projektRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, ProjektRepository projektRepository) {
        this.studentRepository = studentRepository;
        this.projektRepository = projektRepository;
    }

    @Override
    public Optional<Student> getStudent(Integer studentId) {
        return studentRepository.findById(studentId);
    }

    @Override
    @Transactional
    public Student setStudent(Student student) {
        Set<Projekt> wybraneProjekty = pobierzPelneProjekty(student.getProjekty());

        Student zapisanyStudent;
        if (student.getStudentId() != null) {
            zapisanyStudent = studentRepository.findById(student.getStudentId()).orElseThrow();
            zapisanyStudent.setImie(student.getImie());
            zapisanyStudent.setNazwisko(student.getNazwisko());
            zapisanyStudent.setNrIndeksu(student.getNrIndeksu());
            zapisanyStudent.setEmail(student.getEmail());
            zapisanyStudent.setStacjonarny(student.getStacjonarny());
        } else {
            zapisanyStudent = studentRepository.save(student);
        }

        zapisanyStudent = studentRepository.save(zapisanyStudent);
        aktualizujProjektyStudenta(zapisanyStudent, wybraneProjekty);
        zapisanyStudent.setProjekty(wybraneProjekty);
        return zapisanyStudent;
    }

    private Set<Projekt> pobierzPelneProjekty(Set<Projekt> projekty) {
        Set<Projekt> wynik = new HashSet<>();
        if (projekty == null) {
            return wynik;
        }
        for (Projekt p : projekty) {
            if (p != null && p.getProjektId() != null) {
                projektRepository.findById(p.getProjektId()).ifPresent(wynik::add);
            }
        }
        return wynik;
    }

    private void aktualizujProjektyStudenta(Student student, Set<Projekt> wybraneProjekty) {
        for (Projekt projekt : projektRepository.findAll()) {
            if (projekt.getStudenci() == null) {
                projekt.setStudenci(new HashSet<>());
            }
            boolean usunieto = projekt.getStudenci()
                    .removeIf(s -> Objects.equals(s.getStudentId(), student.getStudentId()));
            boolean powinienBycPrzypisany = wybraneProjekty.stream()
                    .anyMatch(p -> Objects.equals(p.getProjektId(), projekt.getProjektId()));
            if (powinienBycPrzypisany) {
                projekt.getStudenci().add(student);
            }
            if (usunieto || powinienBycPrzypisany) {
                projektRepository.save(projekt);
            }
        }
    }

    @Override
    @Transactional
    public void deleteStudent(Integer studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        for (Projekt projekt : projektRepository.findAll()) {
            if (projekt.getStudenci() != null) {
                boolean usunieto = projekt.getStudenci()
                        .removeIf(s -> Objects.equals(s.getStudentId(), studentId));
                if (usunieto) {
                    projektRepository.save(projekt);
                }
            }
        }
        studentRepository.delete(student);
    }

    @Override
    public Page<Student> getStudenci(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Optional<Student> getStudentByNrIndeksu(String nrIndeksu) {
        return studentRepository.findByNrIndeksu(nrIndeksu);
    }

    @Override
    public Page<Student> searchByNrIndeksu(String nrIndeksu, Pageable pageable) {
        return studentRepository.findByNrIndeksuStartsWith(nrIndeksu, pageable);
    }

    @Override
    public Page<Student> searchByNazwisko(String nazwisko, Pageable pageable) {
        return studentRepository.findByNazwiskoStartsWithIgnoreCase(nazwisko, pageable);
    }
}
