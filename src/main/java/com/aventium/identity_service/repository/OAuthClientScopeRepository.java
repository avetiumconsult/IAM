package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.OAuthClientScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OAuthClientScopeRepository extends JpaRepository<OAuthClientScope, UUID> {
    List<OAuthClientScope> findAllByClientId(UUID clientId);
}
