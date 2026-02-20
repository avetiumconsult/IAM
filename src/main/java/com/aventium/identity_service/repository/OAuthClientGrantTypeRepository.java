package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.OAuthClientGrantType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OAuthClientGrantTypeRepository extends JpaRepository<OAuthClientGrantType, UUID> {
    List<OAuthClientGrantType> findAllByClientId(UUID clientId);
}
