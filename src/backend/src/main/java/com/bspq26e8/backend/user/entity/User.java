package com.bspq26e8.backend.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "citext")
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "profile_picture", columnDefinition = "text")
    private String profilePicture;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_preferred_languages",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "language_id")
    private Set<Long> preferredLanguageIds = new HashSet<>();

    protected User() {
    }

    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<Long> getPreferredLanguageIds() {
        return preferredLanguageIds;
    }

    public void setPreferredLanguageIds(Set<Long> preferredLanguageIds) {
        this.preferredLanguageIds = preferredLanguageIds == null ? new HashSet<>() : preferredLanguageIds;
    }
}
