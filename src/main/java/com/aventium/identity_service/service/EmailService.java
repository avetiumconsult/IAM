package com.aventium.identity_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails (verification tokens, password reset tokens, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@identity-service.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:9000}")
    private String baseUrl;

    /**
     * Send email verification token to user
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Email Address";
        String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;
        String body = String.format(
                "Please verify your email address by clicking the link below:\n\n%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.",
                verificationUrl
        );

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send password reset token to user
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Reset Your Password";
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        String body = String.format(
                "You requested to reset your password. Click the link below to reset it:\n\n%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request a password reset, please ignore this email and your password will remain unchanged.",
                resetUrl
        );

        sendEmail(toEmail, subject, body);
    }

    /**
     * Generic email sending method
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            // In production, you might want to throw an exception or queue for retry
            // For now, we log the error but don't fail the request
        }
    }
}
