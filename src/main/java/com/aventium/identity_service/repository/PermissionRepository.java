package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    @Query("select distinct p.name from RolePermission rp join rp.permission p where rp.role.id in :roleIds")
    List<String> findPermissionNamesByRoleIds(Collection<UUID> roleIds);

    @Query("select p from Permission p where p.application.id = :applicationId and p.name = :name")
    Optional<Permission> findByApplicationIdAndName(UUID applicationId, String name);
}
