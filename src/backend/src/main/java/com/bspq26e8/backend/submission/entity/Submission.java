package com.bspq26e8.backend.submission.entity;

import com.bspq26e8.backend.problem.entity.Language;
import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "source_code", nullable = false, columnDefinition = "text")
    private String sourceCode;

    @Convert(converter = SubmissionStatusConverter.class)
    @ColumnTransformer(write = "?::submission_status")
    @Column(name = "status", nullable = false, columnDefinition = "submission_status")
    private SubmissionStatus status;

    @Column(name = "verdict_message", columnDefinition = "text")
    private String verdictMessage;

    @Column(name = "runtime_ms")
    private Integer runtimeMs;

    @Column(name = "memory_mb")
    private Integer memoryMb;

    @Column(name = "testcases_passed", nullable = false)
    private int testcasesPassed = 0;

    @Column(name = "testcases_total")
    private Integer testcasesTotal;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "evaluated_at")
    private OffsetDateTime evaluatedAt;

    protected Submission() {
    }

    public Submission(User user, Problem problem, Language language, String sourceCode) {
        this.user = user;
        this.problem = problem;
        this.language = language;
        this.sourceCode = sourceCode;
        this.status = SubmissionStatus.QUEUED;
        this.submittedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Problem getProblem() {
        return problem;
    }

    public Language getLanguage() {
        return language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public String getVerdictMessage() {
        return verdictMessage;
    }

    public Integer getRuntimeMs() {
        return runtimeMs;
    }

    public Integer getMemoryMb() {
        return memoryMb;
    }

    public int getTestcasesPassed() {
        return testcasesPassed;
    }

    public Integer getTestcasesTotal() {
        return testcasesTotal;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void updateEditableFields(Language language, String sourceCode) {
        if (language != null) {
            this.language = language;
        }

        if (sourceCode != null) {
            this.sourceCode = sourceCode;
        }

        this.status = SubmissionStatus.QUEUED;
        this.verdictMessage = null;
        this.runtimeMs = null;
        this.memoryMb = null;
        this.testcasesPassed = 0;
        this.testcasesTotal = null;
        this.evaluatedAt = null;
        this.submittedAt = OffsetDateTime.now();
    }
}
