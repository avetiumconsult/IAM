package com.aventium.identity_service.security;

import com.aventium.identity_service.entity.OAuthClient;
import com.aventium.identity_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.client.*;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuthClientRepository oauthClientRepository;
    private final OAuthClientScopeRepository scopeRepository;
    private final OAuthClientRedirectUriRepository redirectUriRepository;
    private final OAuthClientGrantTypeRepository grantTypeRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException(
                "RegisteredClient is managed via OAuthClient tables. Create/update clients using your admin workflow."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findById(String id) {
        UUID uuid = UUID.fromString(id);
        OAuthClient client = oauthClientRepository.findById(uuid).orElse(null);
        return (client == null) ? null : toRegisteredClient(client);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findByClientId(String clientId) {
        OAuthClient client = oauthClientRepository.findByClientId(clientId).orElse(null);
        return (client == null) ? null : toRegisteredClient(client);
    }

    private RegisteredClient toRegisteredClient(OAuthClient client) {
        Set<String> scopes = scopeRepository.findAllByClientId(client.getId()).stream()
                .map(s -> s.getScope())
                .collect(Collectors.toSet());

        Set<String> redirectUris = redirectUriRepository.findAllByClientId(client.getId()).stream()
                .map(r -> r.getRedirectUri())
                .collect(Collectors.toSet());

        Set<AuthorizationGrantType> grantTypes = grantTypeRepository.findAllByClientId(client.getId()).stream()
                .map(gt -> new AuthorizationGrantType(gt.getGrantType()))
                .collect(Collectors.toSet());

        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(client.getAccessTokenValidity()))
                .refreshTokenTimeToLive(Duration.ofSeconds(client.getRefreshTokenValidity()))
                .reuseRefreshTokens(true)
                .build();

        return RegisteredClient.withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantTypes(types -> types.addAll(grantTypes))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .scopes(s -> s.addAll(scopes))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false) // Disable PKCE requirement for confidential clients
                        .build())
                .tokenSettings(tokenSettings)
                .build();
    }
}
