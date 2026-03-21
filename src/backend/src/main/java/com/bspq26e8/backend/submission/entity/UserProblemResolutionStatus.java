package com.bspq26e8.backend.submission.entity;

import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
        name = "user_problem_resolution_status",
        uniqueConstraints = @UniqueConstraint(
                name = "user_problem_resolution_status_unique",
                columnNames = {"user_id", "problem_id"}
        )
)
public class UserProblemResolutionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResolutionStatus status = ResolutionStatus.NOT_STARTED;

    protected UserProblemResolutionStatus() {
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

    public ResolutionStatus getStatus() {
        return status;
    }
}

