package com.bspq26e8.backend.submission.repository;

import com.bspq26e8.backend.submission.entity.Submission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
}
