package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.Problem;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    boolean existsBySlug(String slug);
	boolean existsBySlugAndIdNot(String slug, UUID id);

	List<Problem> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

		@Query(value = """
						SELECT DISTINCT p.*
						FROM problems p
						LEFT JOIN users u ON u.id = p.author_id
						WHERE (:language IS NULL OR EXISTS (
								SELECT 1
								FROM problem_languages pl
								JOIN languages l ON l.id = pl.language_id
								WHERE pl.problem_id = p.id
									AND (
										LOWER(l.code) = LOWER(CAST(:language AS text))
										OR LOWER(l.name) = LOWER(CAST(:language AS text))
									)
						))
							AND (:difficulty IS NULL OR p.difficulty = CAST(:difficulty AS problem_difficulty))
							AND (:authorId IS NULL OR p.author_id = :authorId)
							AND (:author IS NULL OR u.username::text ILIKE CONCAT('%', CAST(:author AS text), '%'))
							AND (:name IS NULL OR p.title ILIKE CONCAT('%', CAST(:name AS text), '%'))
						ORDER BY p.created_at DESC
						""", nativeQuery = true)
    List<Problem> findPublicProblems(
	    @Param("language") String language,
	    @Param("difficulty") String difficulty,
	    @Param("authorId") UUID authorId,
						@Param("author") String author,
	    @Param("name") String name
    );
}
