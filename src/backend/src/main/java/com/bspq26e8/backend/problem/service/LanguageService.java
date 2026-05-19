package com.bspq26e8.backend.problem.service;

import com.bspq26e8.backend.problem.repository.LanguageRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LanguageService {

    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    public List<LanguageView> listLanguages() {
        return languageRepository.findAll().stream()
                .map(l -> new LanguageView(l.getId(), l.getName(), l.getCode()))
                .collect(Collectors.toList());
    }

    public record LanguageView(Long id, String name, String code) {}
}
