package com.bspq26e8.backend.problem.entity;

import com.bspq26e8.backend.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "statement_md", nullable = false, columnDefinition = "text")
    private String statementMd;

    @Column(name = "input_spec_md", nullable = false, columnDefinition = "text")
    private String inputSpecMd;

    @Column(name = "output_spec_md", nullable = false, columnDefinition = "text")
    private String outputSpecMd;

    @Column(name = "constraints_md", nullable = false, columnDefinition = "text")
    private String constraintsMd;

    @Column(name = "hints_md", columnDefinition = "text")
    private String hintsMd;

    @Convert(converter = ProblemDifficultyConverter.class)
    @ColumnTransformer(write = "?::problem_difficulty")
    @Column(name = "difficulty", nullable = false, columnDefinition = "problem_difficulty")
    private ProblemDifficulty difficulty;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "solution_template", columnDefinition = "text")
    private String solutionTemplate;



    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "language_compilation_config", nullable = false, columnDefinition = "jsonb")
    private String languageCompilationConfig = "{}";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases;

    @ManyToMany
    @JoinTable (
            name = "problem_tags",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    protected Problem() {
    }

        public Problem(
            String slug,
            String title,
            String statementMd,
            String inputSpecMd,
            String outputSpecMd,
            String constraintsMd,
            String hintsMd,
            ProblemDifficulty difficulty,
            User author,
            String solutionTemplate,
            String languageCompilationConfig
        ) {
        this.slug = slug;
        this.title = title;
        this.statementMd = statementMd;
        this.inputSpecMd = inputSpecMd;
        this.outputSpecMd = outputSpecMd;
        this.constraintsMd = constraintsMd;
        this.hintsMd = hintsMd;
        this.difficulty = difficulty;
        this.author = author;
        this.solutionTemplate = solutionTemplate;
        this.languageCompilationConfig = (languageCompilationConfig == null || languageCompilationConfig.isBlank())
            ? "{}"
            : languageCompilationConfig;
        this.createdAt = OffsetDateTime.now();
        }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getStatementMd() {
        return statementMd;
    }

    public String getInputSpecMd() {
        return inputSpecMd;
    }

    public String getOutputSpecMd() {
        return outputSpecMd;
    }

    public String getConstraintsMd() {
        return constraintsMd;
    }

    public String getHintsMd() {
        return hintsMd;
    }

    public ProblemDifficulty getDifficulty() {
        return difficulty;
    }

    public User getAuthor() {
        return author;
    }

    public String getSolutionTemplate() {
        return solutionTemplate;
    }

    public String getLanguageCompilationConfig() {
        return languageCompilationConfig;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateEditableFields(
            String slug,
            String title,
            String statementMd,
            String inputSpecMd,
            String outputSpecMd,
            String constraintsMd,
            String hintsMd,
            ProblemDifficulty difficulty,
            String solutionTemplate,
            String languageCompilationConfig
    ) {
        if (slug != null) {
            this.slug = slug;
        }
        if (title != null) {
            this.title = title;
        }
        if (statementMd != null) {
            this.statementMd = statementMd;
        }
        if (inputSpecMd != null) {
            this.inputSpecMd = inputSpecMd;
        }
        if (outputSpecMd != null) {
            this.outputSpecMd = outputSpecMd;
        }
        if (constraintsMd != null) {
            this.constraintsMd = constraintsMd;
        }
        if (hintsMd != null) {
            this.hintsMd = hintsMd;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (solutionTemplate != null) {
            this.solutionTemplate = solutionTemplate;
        }
        if (languageCompilationConfig != null) {
            this.languageCompilationConfig = languageCompilationConfig.isBlank() ? "{}" : languageCompilationConfig;
        }
    }
}
