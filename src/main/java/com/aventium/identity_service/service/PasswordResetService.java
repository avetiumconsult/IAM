package com.aventium.identity_service.service;

import com.aventium.identity_service.audit.AuditPublisher;
import com.aventium.identity_service.entity.PasswordResetToken;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.repository.PasswordResetTokenRepository;
import com.aventium.identity_service.repository.UserRepository;
import com.aventium.identity_service.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditPublisher auditPublisher;

    /**
     * Generate and send password reset token
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this email"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User account is not active");
        }

        // Generate token
        String plainToken = UUID.randomUUID().toString();
        String tokenHash = sha256(plainToken);
        Instant expiresAt = Instant.now().plusSeconds(PASSWORD_RESET_TOKEN_EXPIRY_HOURS * 3600);

        // Invalidate any existing tokens for this user
        tokenRepository.findAll().stream()
                .filter(token -> token.getUserId().equals(user.getId()) && token.getConsumedAt() == null)
                .forEach(token -> {
                    token.setConsumedAt(Instant.now());
                    tokenRepository.save(token);
                });

        // Create new token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), plainToken);

        // Audit log
        auditPublisher.publish("PASSWORD_RESET_REQUESTED", user.getId(), null, null,
                java.util.Map.of("email", user.getEmail()));

        // Log token for testing (remove in production)
        System.out.println("=== PASSWORD RESET TOKEN FOR USER: " + user.getEmail() + " ===");
        System.out.println("Token: " + plainToken);
        System.out.println("Use this token with POST /api/v1/auth/reset-password");
        System.out.println("=== COPY TOKEN ABOVE FOR TESTING ===");
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_REQUIRED");
        }

        String tokenHash = sha256(token);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (resetToken.getConsumedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED_OR_USED");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        // Validate password strength
        if (!PasswordPolicy.isStrong(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as consumed
        resetToken.setConsumedAt(Instant.now());
        tokenRepository.save(resetToken);

        // Audit log
        auditPublisher.publish("PASSWORD_RESET_COMPLETED", user.getId(), null, null,
                java.util.Map.of("email", user.getEmail()));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash token", e);
        }
    }
}
