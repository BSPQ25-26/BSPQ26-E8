package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.ProblemLanguage;
import com.bspq26e8.backend.problem.entity.ProblemLanguageId;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, ProblemLanguageId> {

	interface ProblemLanguageView {
		UUID getProblemId();
		String getLanguageName();
		boolean getDefaultLanguage();
	}

	@Query("""
			SELECT pl.problem.id AS problemId,
			       pl.language.name AS languageName,
			       pl.isDefault AS defaultLanguage
			FROM ProblemLanguage pl
			WHERE pl.problem.id IN :problemIds
			ORDER BY pl.problem.id, pl.isDefault DESC, pl.language.name ASC
			""")
	List<ProblemLanguageView> findLanguageRowsByProblemIds(@Param("problemIds") Collection<UUID> problemIds);
}
