package com.aventium.identity_service.controller;

import com.aventium.identity_service.audit.AuditPublisher;
import com.aventium.identity_service.repository.EmailVerificationTokenRepository;
import com.aventium.identity_service.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class VerifyEmailController {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final AuditPublisher auditPublisher;

    public VerifyEmailController(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository, AuditPublisher auditPublisher) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.auditPublisher = auditPublisher;
    }

    public static class VerifyRequest { @NotBlank public String token; }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Verify email address", description = "Verifies a user's email address using the verification token sent to their email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid, expired, or already used token")
    })
    public void verify(@RequestBody VerifyRequest body) {
        String token = body == null ? null : body.token;
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_REQUIRED");
        }
        String hash = sha256(token);
        var ev = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (ev.getConsumedAt() != null || ev.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED_OR_USED");
        }
        var user = userRepository.findById(ev.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));
        user.setActive(true);
        // optional: add a boolean emailVerified; for now we assume activation represents verification
        userRepository.save(user);

        ev.setConsumedAt(Instant.now());
        tokenRepository.save(ev);
        auditPublisher.publish("EMAIL_VERIFIED", user.getId(), null, null, java.util.Map.of("email", user.getEmail()));
    }

    static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
