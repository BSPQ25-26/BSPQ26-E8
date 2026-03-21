package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "test_cases")
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name="problem_id", nullable = false)
    private Problem problem;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputData;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean isSample = false;





}
