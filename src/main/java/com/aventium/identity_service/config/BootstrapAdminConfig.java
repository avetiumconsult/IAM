package com.aventium.identity_service.config;

import com.aventium.identity_service.entity.Application;
import com.aventium.identity_service.entity.Role;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.entity.UserApplicationRole;
import com.aventium.identity_service.repository.ApplicationRepository;
import com.aventium.identity_service.repository.RoleRepository;
import com.aventium.identity_service.repository.UserApplicationRoleRepository;
import com.aventium.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Bootstrap configuration to create the first admin user.
 * 
 * Admin Registration & Login Flow:
 * 1. First admin is created via this bootstrap (or manually via database)
 * 2. Admin registers/login same way as regular users via /api/v1/auth/register
 * 3. Admin logs in via OAuth flow (same as regular users)
 * 4. Admin gets JWT token with ADMIN roles
 * 5. Admin can then assign roles to other users via /api/v1/admin/users/roles
 * 
 * To create first admin, set these environment variables:
 * - BOOTSTRAP_ADMIN_USERNAME=admin
 * - BOOTSTRAP_ADMIN_EMAIL=admin@example.com
 * - BOOTSTRAP_ADMIN_PASSWORD=SecurePassword123!
 */
@Configuration
@Profile("dev") // Only runs in dev profile
@RequiredArgsConstructor
public class BootstrapAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminConfig.class);

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;
    private final UserApplicationRoleRepository userApplicationRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Bean
    CommandLineRunner bootstrapAdmin() {
        return args -> {
            String adminUsername = environment.getProperty("BOOTSTRAP_ADMIN_USERNAME", "admin");
            String adminEmail = environment.getProperty("BOOTSTRAP_ADMIN_EMAIL", "admin@example.com");
            String adminPassword = environment.getProperty("BOOTSTRAP_ADMIN_PASSWORD", "Admin123!@#");

            createBootstrapAdmin(adminUsername, adminEmail, adminPassword);
        };
    }

    @Transactional
    void createBootstrapAdmin(String username, String email, String password) {
        // Check if admin already exists
        Optional<User> existingAdmin = userRepository.findByUsername(username.toLowerCase());
        if (existingAdmin.isPresent()) {
            log.info("Bootstrap admin '{}' already exists. Skipping creation.", username);
            return;
        }

        log.info("Creating bootstrap admin user: {}", username);

        // Create admin user
        User admin = new User();
        admin.setUsername(username.toLowerCase());
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setActive(true);
        admin.setLocked(false);
        admin.setFailedAttempts(0);
        admin = userRepository.save(admin);

        log.info("Created admin user: {} (ID: {})", username, admin.getId());

        // Assign ADMIN roles to all applications
        List<Application> applications = applicationRepository.findAll();
        for (Application app : applications) {
            // Find ADMIN role for this application (e.g., POS_ADMIN, PROC_ADMIN)
            String adminRoleName = app.getName().toUpperCase() + "_ADMIN";
            Optional<Role> adminRole = roleRepository.findByApplicationIdAndName(app.getId(), adminRoleName);

            if (adminRole.isPresent()) {
                // Check if already assigned
                boolean alreadyAssigned = userApplicationRoleRepository
                        .findAllByUserIdAndApplicationIdWithRole(admin.getId(), app.getId())
                        .stream()
                        .anyMatch(uar -> uar.getRole().getName().equals(adminRoleName));

                if (!alreadyAssigned) {
                    UserApplicationRole uar = UserApplicationRole.builder()
                            .user(admin)
                            .role(adminRole.get())
                            .assignedAt(Instant.now())
                            .build();
                    userApplicationRoleRepository.save(uar);
                    log.info("Assigned {} role to admin for application: {}", adminRoleName, app.getName());
                }
            } else {
                log.warn("Admin role '{}' not found for application '{}'. Skipping role assignment.", 
                        adminRoleName, app.getName());
            }
        }

        log.info("Bootstrap admin setup complete. Admin can now login via OAuth and manage users.");
        log.info("Admin credentials - Username: {}, Email: {}", username, email);
    }
}
