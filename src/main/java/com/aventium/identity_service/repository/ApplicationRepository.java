package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    Optional<Application> findByName(String name);
}
