package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.OAuthClientRedirectUri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OAuthClientRedirectUriRepository extends JpaRepository<OAuthClientRedirectUri, UUID> {
    List<OAuthClientRedirectUri> findAllByClientId(UUID clientId);
}
