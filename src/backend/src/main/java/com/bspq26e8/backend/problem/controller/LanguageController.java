package com.bspq26e8.backend.problem.controller;

import com.bspq26e8.backend.problem.service.LanguageService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    public ResponseEntity<List<LanguageService.LanguageView>> listLanguages() {
        return ResponseEntity.ok(languageService.listLanguages());
    }
}
