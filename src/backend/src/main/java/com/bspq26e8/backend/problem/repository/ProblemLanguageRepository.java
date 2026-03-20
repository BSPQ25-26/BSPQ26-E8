package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.ProblemLanguage;
import com.bspq26e8.backend.problem.entity.ProblemLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, ProblemLanguageId> {
}
