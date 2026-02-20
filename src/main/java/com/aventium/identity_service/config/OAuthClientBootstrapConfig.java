package com.aventium.identity_service.config;

import com.aventium.identity_service.entity.Application;
import com.aventium.identity_service.entity.OAuthClient;
import com.aventium.identity_service.entity.OAuthClientGrantType;
import com.aventium.identity_service.entity.OAuthClientRedirectUri;
import com.aventium.identity_service.entity.OAuthClientScope;
import com.aventium.identity_service.repository.ApplicationRepository;
import com.aventium.identity_service.repository.OAuthClientGrantTypeRepository;
import com.aventium.identity_service.repository.OAuthClientRedirectUriRepository;
import com.aventium.identity_service.repository.OAuthClientRepository;
import com.aventium.identity_service.repository.OAuthClientScopeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bootstrap configuration to create OAuth clients for all applications.
 * 
 * Standard OAuth2/OIDC Scopes:
 * - openid: Required for OpenID Connect (enables ID token)
 * - profile: User's profile information (name, username, etc.)
 * - email: User's email address
 * - phone: User's phone number (optional)
 * - address: User's address (optional)
 * 
 * Each application gets:
 * - client_id: {app-name}-client (e.g., "pos-client", "payroll-client")
 * - client_secret: Auto-generated secure secret
 * - redirect_uri: http://localhost:3000/callback (default, can be customized)
 * - grant_types: authorization_code, refresh_token
 * - scopes: openid, profile, email
 */
@Configuration
@Profile("dev") // Only runs in dev profile
@RequiredArgsConstructor
public class OAuthClientBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientBootstrapConfig.class);

    // Standard OAuth2/OIDC scopes
    private static final List<String> STANDARD_SCOPES = List.of(
            "openid",      // Required for OpenID Connect
            "profile",     // User profile information
            "email"        // User email address
    );

    // Grant types for authorization code flow
    private static final List<String> GRANT_TYPES = List.of(
            "authorization_code",  // Standard OAuth2 authorization code flow
            "refresh_token"        // Allow token refresh
    );

    // Default redirect URI (can be customized per application)
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:3000/callback";

    // Token validity (in seconds)
    private static final int ACCESS_TOKEN_VALIDITY = 3600;      // 1 hour
    private static final int REFRESH_TOKEN_VALIDITY = 86400;    // 24 hours

    private final ApplicationRepository applicationRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final OAuthClientScopeRepository scopeRepository;
    private final OAuthClientRedirectUriRepository redirectUriRepository;
    private final OAuthClientGrantTypeRepository grantTypeRepository;

    @Bean
    CommandLineRunner bootstrapOAuthClients() {
        return args -> createOAuthClientsForAllApplications();
    }

    @Transactional
    void createOAuthClientsForAllApplications() {
        List<Application> applications = applicationRepository.findAll();

        if (applications.isEmpty()) {
            log.warn("No applications found. OAuth clients cannot be created without applications.");
            return;
        }

        log.info("Creating OAuth clients for {} application(s)...", applications.size());

        for (Application app : applications) {
            createOAuthClientForApplication(app);
        }

        log.info("OAuth client bootstrap complete. Created clients for all applications.");
    }

    private void createOAuthClientForApplication(Application app) {
        String clientId = app.getName().toLowerCase() + "-client";
        
        // Check if client already exists
        Optional<OAuthClient> existing = oauthClientRepository.findByClientId(clientId);
        if (existing.isPresent()) {
            log.info("OAuth client '{}' already exists for application '{}'. Skipping.", clientId, app.getName());
            return;
        }

        log.info("Creating OAuth client for application: {}", app.getName());

        // Generate client secret (store as-is, not hashed - OAuth2 requires raw secret for verification)
        String clientSecret = UUID.randomUUID().toString().replace("-", "") + 
                             UUID.randomUUID().toString().replace("-", "");

        // Create OAuth client
        OAuthClient client = OAuthClient.builder()
                .clientId(clientId)
                .clientSecret(clientSecret) // Store raw secret (OAuth2 standard)
                .application(app)
                .accessTokenValidity(ACCESS_TOKEN_VALIDITY)
                .refreshTokenValidity(REFRESH_TOKEN_VALIDITY)
                .build();

        OAuthClient savedClient = oauthClientRepository.save(client);

        // Create scopes
        for (String scope : STANDARD_SCOPES) {
            OAuthClientScope clientScope = OAuthClientScope.builder()
                    .client(savedClient)
                    .scope(scope)
                    .build();
            scopeRepository.save(clientScope);
            log.debug("  - Added scope: {}", scope);
        }

        // Create redirect URI
        OAuthClientRedirectUri redirectUri = OAuthClientRedirectUri.builder()
                .client(savedClient)
                .redirectUri(DEFAULT_REDIRECT_URI)
                .build();
        redirectUriRepository.save(redirectUri);
        log.debug("  - Added redirect URI: {}", DEFAULT_REDIRECT_URI);

        // Create grant types
        for (String grantType : GRANT_TYPES) {
            OAuthClientGrantType clientGrantType = OAuthClientGrantType.builder()
                    .client(savedClient)
                    .grantType(grantType)
                    .build();
            grantTypeRepository.save(clientGrantType);
            log.debug("  - Added grant type: {}", grantType);
        }

        // Log client credentials (IMPORTANT: Save these securely!)
        log.info("=== OAUTH CLIENT CREATED FOR APPLICATION: {} ===", app.getName());
        log.info("Client ID: {}", clientId);
        log.info("Client Secret (RAW - SAVE THIS): {}", clientSecret);
        log.info("Redirect URI: {}", DEFAULT_REDIRECT_URI);
        log.info("Scopes: {}", String.join(", ", STANDARD_SCOPES));
        log.info("Grant Types: {}", String.join(", ", GRANT_TYPES));
        log.info("Authorization URL: http://localhost:9000/oauth2/authorize?client_id={}&response_type=code&redirect_uri={}&scope=openid%20profile%20email", 
                clientId, DEFAULT_REDIRECT_URI);
        log.info("=== SAVE CLIENT SECRET ABOVE - IT WON'T BE SHOWN AGAIN ===");
    }
}
