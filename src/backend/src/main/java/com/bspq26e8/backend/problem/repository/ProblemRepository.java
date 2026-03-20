package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.Problem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {
}
