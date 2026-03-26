package com.bspq26e8.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "text")
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(User user, String tokenHash, OffsetDateTime expiresAt, String ipAddress, String userAgent) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void revokeNow() {
        this.revokedAt = OffsetDateTime.now();
    }
}
