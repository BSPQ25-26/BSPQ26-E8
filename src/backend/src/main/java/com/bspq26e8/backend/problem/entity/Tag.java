package com.bspq26e8.backend.problem.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, unique = true, length = 60)
    private String slug;

    @Column(name = "name", nullable = false, unique = true, length = 80)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<Problem> problems;
    protected Tag() {
    }

    public Tag(String slug, String name) {
        this.slug = slug;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }
}
