package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.RolePermission;
import com.aventium.identity_service.util.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Id> {
}
