package com.aventium.identity_service.security;

import com.aventium.identity_service.entity.OAuthClient;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.repository.OAuthClientRepository;
import com.aventium.identity_service.repository.UserApplicationRoleRepository;
import com.aventium.identity_service.repository.UserRepository;
import com.aventium.identity_service.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JwtClaimsConfig {

    private final OAuthClientRepository oauthClientRepository;
    private final UserRepository userRepository;
    private final UserApplicationRoleRepository userApplicationRoleRepository;
    private final PermissionRepository permissionRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return this::customize;
    }

    @Transactional(readOnly = true)
    void customize(JwtEncodingContext context) {
        // Only enrich JWTs (access_token, id_token)
        if (!"JWT".equalsIgnoreCase(context.getTokenType().getValue())
                && !"id_token".equalsIgnoreCase(context.getTokenType().getValue())
                && !"access_token".equalsIgnoreCase(context.getTokenType().getValue())) {
            return;
        }

        String clientId = context.getRegisteredClient().getClientId();
        OAuthClient client = oauthClientRepository.findByClientId(clientId).orElse(null);
        if (client == null) return;

        String username = context.getPrincipal().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return;

        // Get user's roles for this specific application
        // Note: If user has no roles yet, this returns empty list - token will have empty roles/permissions
        // Admin can assign roles later, and user will get them on next login
        var uarList = userApplicationRoleRepository
                .findAllByUserIdAndApplicationIdWithRole(user.getId(), client.getApplication().getId());

        List<String> rolesForApp = uarList.stream()
                .map(uar -> uar.getRole().getName())
                .distinct()
                .toList();

        List<UUID> roleIds = uarList.stream()
                .map(uar -> uar.getRole().getId())
                .distinct()
                .toList();

        // Empty list if no roles - allows users to authenticate before role assignment
        List<String> permsForApp = roleIds.isEmpty() ? List.of() : permissionRepository.findPermissionNamesByRoleIds(roleIds);

        context.getClaims().claim("application", client.getApplication().getName());
        context.getClaims().claim("roles", rolesForApp); // Can be empty []
        context.getClaims().claim("perms", permsForApp); // Can be empty []
        context.getClaims().claim("client_id", clientId);
    }
}
