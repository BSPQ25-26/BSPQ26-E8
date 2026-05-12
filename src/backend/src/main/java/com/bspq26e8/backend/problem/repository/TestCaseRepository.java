package com.bspq26e8.backend.problem.repository;

import com.bspq26e8.backend.problem.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {
}
