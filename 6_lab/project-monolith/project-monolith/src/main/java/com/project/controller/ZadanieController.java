package com.project.controller;

import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.service.ProjektService;
import com.project.service.ZadanieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ZadanieController {

    private final ZadanieService zadanieService;
    private final ProjektService projektService;

    public ZadanieController(ZadanieService zadanieService, ProjektService projektService) {
        this.zadanieService = zadanieService;
        this.projektService = projektService;
    }

    @GetMapping("/zadanieList")
    public String zadanieList(Model model,
                              @PageableDefault(sort = "kolejnosc", direction = Sort.Direction.ASC) Pageable pageable) {
        model.addAttribute("zadania", zadanieService.getZadania(pageable).getContent());
        return "zadanieList";
    }

    @GetMapping("/zadanieEdit")
    public String zadanieEdit(@RequestParam(name = "zadanieId", required = false) Integer zadanieId, Model model) {
        Zadanie zadanie = (zadanieId != null)
                ? zadanieService.getZadanie(zadanieId).orElseThrow()
                : new Zadanie();
        model.addAttribute("zadanie", zadanie);
        dodajListeProjektow(model);
        return "zadanieEdit";
    }

    @PostMapping(path = "/zadanieEdit")
    public String zadanieEditSave(@ModelAttribute @Valid Zadanie zadanie,
                                  BindingResult bindingResult,
                                  @RequestParam(name = "projektId", required = false) Integer projektId,
                                  Model model) {
        if (projektId != null) {
            projektService.getProjekt(projektId).ifPresent(zadanie::setProjekt);
        } else {
            zadanie.setProjekt(null);
        }

        if (bindingResult.hasErrors()) {
            dodajListeProjektow(model);
            return "zadanieEdit";
        }

        try {
            zadanieService.setZadanie(zadanie);
        } catch (com.project.exception.HttpException e) {
            bindingResult.rejectValue("", "400", "API odrzuciło dane: " + e.getMessage());
            dodajListeProjektow(model);
            return "zadanieEdit";
        }
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "cancel", path = "/zadanieEdit")
    public String zadanieEditCancel() {
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "delete", path = "/zadanieEdit")
    public String zadanieEditDelete(@ModelAttribute Zadanie zadanie) {
        zadanieService.deleteZadanie(zadanie.getZadanieId());
        return "redirect:/zadanieList";
    }

    private void dodajListeProjektow(Model model) {
        model.addAttribute("projekty", projektService.getProjekty(PageRequest.of(0, 100, Sort.by("nazwa"))).getContent());
    }
}
