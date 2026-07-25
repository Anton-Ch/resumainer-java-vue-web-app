package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Hashed auth token for email verification and password reset.
 *
 * <p>Maps to the 'auth_tokens' table (BIGSERIAL PK).
 * Raw tokens are never persisted — only the cryptographic hash is stored.
 */
public class AuthToken {

    private Long id;
    private String userId;       // UUID as string (FK → users.id)
    private String tokenType;    // EMAIL_VERIFICATION or PASSWORD_RESET
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createdAt;

    public AuthToken() {
    }

    public AuthToken(String userId, String tokenType, String tokenHash, LocalDateTime expiresAt) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tokenType = Objects.requireNonNull(tokenType, "tokenType must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(LocalDateTime consumedAt) { this.consumedAt = consumedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isConsumed() { return consumedAt != null; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}
