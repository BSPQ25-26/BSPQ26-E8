package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "languages")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 200)
    private String code;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "compile_cmd", columnDefinition = "text")
    private String compileCmd;

    @Column(name = "run_cmd",columnDefinition = "text")
    private String runCmd;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Language() {
    }

    public Language(String code, String name, String compileCmd, String runCmd, OffsetDateTime createdAt) {
        this.code = code;
        this.name = name;
        this.compileCmd = compileCmd;
        this.runCmd = runCmd;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCompileCmd() {
        return compileCmd;
    }

    public String getRunCmd() {
        return runCmd;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

