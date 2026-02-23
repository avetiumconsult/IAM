package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByApplicationId(UUID applicationId);

    Optional<Role> findByApplicationIdAndName(UUID applicationId, String name);
}
