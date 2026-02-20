package com.aventium.identity_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for password reset tokens.
 * Tokens are hashed (SHA-256) before storage for security.
 */
@Entity
@Table(
        name = "password_reset_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_password_reset_token_hash", columnNames = {"token_hash"})
        },
        indexes = {
                @Index(name = "ix_password_reset_user", columnList = "user_id"),
                @Index(name = "ix_password_reset_expires", columnList = "expires_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash; // SHA-256 hex

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

}
