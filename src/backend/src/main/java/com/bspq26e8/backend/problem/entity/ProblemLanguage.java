package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "problem_languages")
public class ProblemLanguage {

    @EmbeddedId
    private ProblemLanguageId id;

    @ManyToOne(optional = false)
    @MapsId("problemId")
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(optional = false)
    @MapsId("languageId")
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "compilation_args", columnDefinition = "text")
    private String compilationArgs;

    @Column(name = "execution_args", columnDefinition = "text")
    private String executionArgs;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    protected ProblemLanguage() {
    }

    public ProblemLanguageId getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public Language getLanguage() {
        return language;
    }

    public String getCompilationArgs() {
        return compilationArgs;
    }

    public String getExecutionArgs() {
        return executionArgs;
    }

    public boolean isDefault() {
        return isDefault;
    }
}

