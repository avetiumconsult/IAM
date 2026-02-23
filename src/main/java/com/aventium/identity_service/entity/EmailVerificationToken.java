package com.aventium.identity_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Defines the JPA entity and repository for email verification tokens.
 * The entity is package-private and the repository is public to satisfy Java's single-public-type-per-file rule
 * while allowing us to introduce both components in one step.
 */

@Entity
@Table(
        name = "email_verification_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_email_verification_token_hash", columnNames = {"token_hash"})
        },
        indexes = {
                @Index(name = "ix_email_verification_user", columnList = "user_id"),
                @Index(name = "ix_email_verification_expires", columnList = "expires_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

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