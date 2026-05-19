package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.Language;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByCodeIgnoreCase(String code);
}
