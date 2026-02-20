package com.aventium.identity_service.service;

import com.aventium.identity_service.audit.AuditPublisher;
import com.aventium.identity_service.entity.Role;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.entity.UserApplicationRole;
import com.aventium.identity_service.repository.ApplicationRepository;
import com.aventium.identity_service.repository.RoleRepository;
import com.aventium.identity_service.repository.UserApplicationRoleRepository;
import com.aventium.identity_service.repository.UserRepository;
import com.aventium.identity_service.security.AdminAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;
    private final UserApplicationRoleRepository userApplicationRoleRepository;
    private final AdminAuthorizationService adminAuthorizationService;
    private final AuditPublisher auditPublisher;

    @Transactional
    public UserApplicationRole assignRoleToUser(UUID userId, String applicationName, String roleName, HttpServletRequest request) {
        // Check if current user has ADMIN role for this application
        adminAuthorizationService.requireAdminRoleForApplication(applicationName);

        // Get current admin user for audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        User adminUser = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Admin user not found"));

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Find application
        var application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found: " + applicationName));

        // Find role for this application
        Role role = roleRepository.findByApplicationIdAndName(application.getId(), roleName)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, 
                        "Role '" + roleName + "' not found for application '" + applicationName + "'"));

        // Check if user already has this role
        var existing = userApplicationRoleRepository.findAllByUserIdAndApplicationIdWithRole(userId, application.getId())
                .stream()
                .filter(uar -> uar.getRole().getId().equals(role.getId()))
                .findFirst();

        if (existing.isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, 
                    "User already has role '" + roleName + "' in application '" + applicationName + "'");
        }

        // Assign role
        UserApplicationRole userApplicationRole = UserApplicationRole.builder()
                .user(user)
                .role(role)
                .assignedAt(Instant.now())
                .build();

        UserApplicationRole saved = userApplicationRoleRepository.save(userApplicationRole);

        // Audit log
        auditPublisher.publish(
                "ROLE_ASSIGNED",
                adminUser.getId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                Map.of(
                        "targetUserId", userId,
                        "targetUsername", user.getUsername(),
                        "application", applicationName,
                        "role", roleName
                )
        );

        return saved;
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, String applicationName, String roleName, HttpServletRequest request) {
        // Check if current user has ADMIN role for this application
        adminAuthorizationService.requireAdminRoleForApplication(applicationName);

        // Get current admin user for audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        User adminUser = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Admin user not found"));

        // Verify user exists
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Find application
        var application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found: " + applicationName));

        // Find role
        Role role = roleRepository.findByApplicationIdAndName(application.getId(), roleName)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, 
                        "Role '" + roleName + "' not found for application '" + applicationName + "'"));

        // Find and remove the assignment
        var userApplicationRole = userApplicationRoleRepository.findAllByUserIdAndApplicationIdWithRole(userId, application.getId())
                .stream()
                .filter(uar -> uar.getRole().getId().equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, 
                        "User does not have role '" + roleName + "' in application '" + applicationName + "'"));

        userApplicationRoleRepository.delete(userApplicationRole);

        // Audit log
        auditPublisher.publish(
                "ROLE_REMOVED",
                adminUser.getId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                Map.of(
                        "targetUserId", userId,
                        "targetUsername", targetUser.getUsername(),
                        "application", applicationName,
                        "role", roleName
                )
        );
    }
}
