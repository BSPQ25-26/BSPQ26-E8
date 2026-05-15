package com.bspq26e8.backend.problem.controller;

import com.bspq26e8.backend.problem.entity.Language;
import com.bspq26e8.backend.problem.repository.LanguageRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private final LanguageRepository languageRepository;

    public LanguageController(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping
    public ResponseEntity<List<LanguageView>> listLanguages() {
        List<LanguageView> languages = languageRepository.findAll().stream()
                .map(l -> new LanguageView(l.getId(), l.getName(), l.getCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(languages);
    }

    public record LanguageView(Long id, String name, String code) {
    }
}
