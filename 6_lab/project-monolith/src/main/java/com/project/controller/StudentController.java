package com.project.controller;

import com.project.model.Projekt;
import com.project.model.Student;
import com.project.service.ProjektService;
import com.project.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class StudentController {

    private final StudentService studentService;
    private final ProjektService projektService;

    public StudentController(StudentService studentService, ProjektService projektService) {
        this.studentService = studentService;
        this.projektService = projektService;
    }

    @GetMapping("/studentList")
    public String studentList(@RequestParam(name = "nazwisko", required = false) String nazwisko,
                              Model model,
                              @PageableDefault(size = 10, sort = "nazwisko", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Student> studentPage = (nazwisko != null && !nazwisko.isBlank())
                ? studentService.searchByNazwisko(nazwisko, pageable)
                : studentService.getStudenci(pageable);

        model.addAttribute("studentPage", studentPage);
        model.addAttribute("studenci", studentPage.getContent());
        model.addAttribute("nazwisko", nazwisko);
        return "studentList";
    }

    @GetMapping("/studentEdit")
    public String studentEdit(@RequestParam(name = "studentId", required = false) Integer studentId, Model model) {
        Student student = (studentId != null)
                ? studentService.getStudent(studentId).orElseThrow()
                : new Student();
        if (student.getProjekty() == null) {
            student.setProjekty(new HashSet<>());
        }
        model.addAttribute("student", student);
        dodajListeProjektow(model);
        dodajWybraneProjektIds(model, student.getProjekty());
        return "studentEdit";
    }

    @PostMapping(path = "/studentEdit")
    public String studentEditSave(@ModelAttribute @Valid Student student,
                                  BindingResult bindingResult,
                                  @RequestParam(name = "projektIds", required = false) List<Integer> projektIds,
                                  Model model) {
        student.setProjekty(pobierzProjekty(projektIds));

        if (bindingResult.hasErrors()) {
            dodajListeProjektow(model);
            dodajWybraneProjektIds(model, student.getProjekty());
            return "studentEdit";
        }

        try {
            studentService.setStudent(student);
        } catch (com.project.exception.HttpException e) {
            bindingResult.rejectValue("", "400", "Błąd API: " + e.getMessage());
            dodajListeProjektow(model);
            dodajWybraneProjektIds(model, student.getProjekty());
            return "studentEdit";
        }
        return "redirect:/studentList";
    }

    @PostMapping(params = "cancel", path = "/studentEdit")
    public String studentEditCancel() {
        return "redirect:/studentList";
    }

    @PostMapping(params = "delete", path = "/studentEdit")
    public String studentEditDelete(@ModelAttribute Student student) {
        studentService.deleteStudent(student.getStudentId());
        return "redirect:/studentList";
    }

    private void dodajListeProjektow(Model model) {
        model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100, Sort.by("nazwa"))).getContent());
    }
    private void dodajWybraneProjektIds(Model model, Set<Projekt> projektyStudenta) {
        Set<Integer> wybraneProjektIds = new HashSet<>();
        if (projektyStudenta != null) {
            for (Projekt projekt : projektyStudenta) {
                if (projekt != null && projekt.getProjektId() != null) {
                    wybraneProjektIds.add(projekt.getProjektId());
                }
            }
        }
        model.addAttribute("wybraneProjektIds", wybraneProjektIds);
    }
    private Set<Projekt> pobierzProjekty(List<Integer> projektIds) {
        Set<Projekt> projekty = new HashSet<>();
        if (projektIds == null) {
            return projekty;
        }
        for (Integer projektId : projektIds) {
            if (projektId != null) {
                projektService.getProjekt(projektId).ifPresent(projekty::add);
            }
        }
        return projekty;
    }
}
