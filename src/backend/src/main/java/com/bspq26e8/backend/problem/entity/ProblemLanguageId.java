package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProblemLanguageId implements Serializable {

    @Column(name = "problem_id", nullable = false)
    private UUID problemId;

    @Column(name = "language_id", nullable = false)
    private Long languageId;

    protected ProblemLanguageId() {
    }

    public ProblemLanguageId(UUID problemId, Long languageId) {
        this.problemId = problemId;
        this.languageId = languageId;
    }

    public UUID getProblemId() {
        return problemId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemLanguageId that = (ProblemLanguageId) o;
        return Objects.equals(problemId, that.problemId) && Objects.equals(languageId, that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(problemId, languageId);
    }
}
