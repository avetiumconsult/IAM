package com.aventium.identity_service.security;

import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.repository.ApplicationRepository;
import com.aventium.identity_service.repository.UserApplicationRoleRepository;
import com.aventium.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Service to check if the current authenticated user has ADMIN role for an application.
 * Used to authorize admin actions like assigning roles to users.
 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final UserApplicationRoleRepository userApplicationRoleRepository;

    /**
     * Checks if the current authenticated user has ADMIN role for the specified application.
     * Throws FORBIDDEN if user is not authenticated or doesn't have ADMIN role.
     */
    @Transactional(readOnly = true)
    public void requireAdminRoleForApplication(String applicationName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(FORBIDDEN, "Authentication required");
        }

        String usernameOrEmail = authentication.getName();
        User currentUser = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "User not found"));

        var application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Application not found: " + applicationName));

        // Check if user has ADMIN role for this application
        List<String> userRoles = userApplicationRoleRepository
                .findAllByUserIdAndApplicationIdWithRole(currentUser.getId(), application.getId())
                .stream()
                .map(uar -> uar.getRole().getName())
                .toList();

        String adminRoleName = applicationName.toUpperCase() + "_ADMIN";
        boolean hasAdminRole = userRoles.stream()
                .anyMatch(role -> role.equalsIgnoreCase(adminRoleName));

        if (!hasAdminRole) {
            throw new ResponseStatusException(FORBIDDEN, 
                    "User does not have ADMIN role for application '" + applicationName + "'. Required role: " + adminRoleName);
        }
    }
}
