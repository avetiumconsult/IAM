package com.aventium.identity_service.service;

import com.aventium.identity_service.audit.AuditPublisher;
import com.aventium.identity_service.dto.RegistrationRequest;
import com.aventium.identity_service.entity.EmailVerificationToken;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.repository.EmailVerificationTokenRepository;
import com.aventium.identity_service.repository.UserRepository;
import com.aventium.identity_service.service.EmailService;
import com.aventium.identity_service.util.PasswordPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);
    private static final int EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditPublisher auditPublisher;
    private final EmailService emailService;

    public RegistrationServiceImpl(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            AuditPublisher auditPublisher,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditPublisher = auditPublisher;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public User register(RegistrationRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USERNAME_TAKEN");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_TAKEN");
        }
        if (!PasswordPolicy.isStrong(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(false); // User must verify email first
        user.setLocked(false);
        user.setFailedAttempts(0);

        User saved = userRepository.save(user);

        // Generate email verification token
        String plainToken = UUID.randomUUID().toString();
        String tokenHash = sha256(plainToken);
        Instant expiresAt = Instant.now().plusSeconds(EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS * 3600);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(saved.getId())
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        emailVerificationTokenRepository.save(verificationToken);

        // Send verification email
        try {
            emailService.sendVerificationEmail(saved.getEmail(), plainToken);
            log.info("Verification email sent to: {}", saved.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send verification email to: {}. Token logged for testing.", saved.getEmail(), e);
            // Log token for testing if email fails
            log.info("=== EMAIL VERIFICATION TOKEN FOR USER: {} ===", saved.getUsername());
            log.info("Email: {}", saved.getEmail());
            log.info("Verification Token: {}", plainToken);
            log.info("Use this token with POST /api/v1/auth/verify-email");
            log.info("=== COPY TOKEN ABOVE FOR TESTING ===");
        }

        auditPublisher.publish("USER_REGISTERED", saved.getId(), null, null, java.util.Map.of("email", email));
        return saved;
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase();
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
