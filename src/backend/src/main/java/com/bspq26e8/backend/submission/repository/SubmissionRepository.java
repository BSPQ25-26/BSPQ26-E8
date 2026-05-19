package com.bspq26e8.backend.submission.repository;

import com.bspq26e8.backend.submission.entity.Submission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    @Query(value = """
	    SELECT s.id
	    FROM submissions s
	    WHERE s.status = CAST('queued' AS submission_status)
	    ORDER BY s.submitted_at ASC
	    LIMIT :limit
	    """, nativeQuery = true)
    List<UUID> findQueuedIds(@Param("limit") int limit);

    @Query("""
	    SELECT DISTINCT s
	    FROM Submission s
	    JOIN FETCH s.user
	    JOIN FETCH s.problem p
	    JOIN FETCH s.language
	    LEFT JOIN FETCH p.testCases
	    WHERE s.id = :submissionId
	    """)
    Optional<Submission> findByIdWithExecutionData(@Param("submissionId") UUID submissionId);

    @Query(value = """
	    SELECT s.*
	    FROM submissions s
	    WHERE s.user_id = :userId
	      AND (:problemId IS NULL OR s.problem_id = :problemId)
	      AND (:status IS NULL OR s.status = CAST(:status AS submission_status))
	    ORDER BY s.submitted_at DESC
	    """, nativeQuery = true)
    List<Submission> findMine(
	    @Param("userId") UUID userId,
	    @Param("problemId") UUID problemId,
	    @Param("status") String status
    );

    @Query(value = """
	    SELECT s.*
	    FROM submissions s
	    WHERE s.problem_id = :problemId
	      AND (:status IS NULL OR s.status = CAST(:status AS submission_status))
	    ORDER BY s.submitted_at DESC
	    """, nativeQuery = true)
    List<Submission> findByProblem(
	    @Param("problemId") UUID problemId,
	    @Param("status") String status
    );

    @Query(value = """
	    SELECT s.*
	    FROM submissions s
	    WHERE s.user_id = :userId
	      AND (:problemId IS NULL OR s.problem_id = :problemId)
	    ORDER BY s.submitted_at DESC
	    LIMIT 1
	    """, nativeQuery = true)
    Optional<Submission> findLatestMine(
	    @Param("userId") UUID userId,
	    @Param("problemId") UUID problemId
    );

    @Query(value = """
	    SELECT s.*
	    FROM submissions s
	    WHERE s.user_id = :userId
	      AND s.problem_id = :problemId
	    ORDER BY
	      CASE WHEN s.status::text = 'accepted' THEN 1 ELSE 0 END DESC,
	      s.testcases_passed DESC,
	      COALESCE(s.runtime_ms, 2147483647) ASC,
	      COALESCE(s.memory_mb, 2147483647) ASC,
	      s.submitted_at DESC
	    LIMIT 1
	    """, nativeQuery = true)
    Optional<Submission> findBestMineByProblem(
	    @Param("userId") UUID userId,
	    @Param("problemId") UUID problemId
    );
}
