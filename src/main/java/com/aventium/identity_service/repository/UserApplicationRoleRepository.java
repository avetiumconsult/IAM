package com.aventium.identity_service.repository;

import com.aventium.identity_service.entity.UserApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserApplicationRoleRepository extends JpaRepository<UserApplicationRole, UUID> {

    @Query("""
            select uar
            from UserApplicationRole uar
            join fetch uar.role r
            join fetch r.application a
            where uar.user.id = :userId
            """)
    List<UserApplicationRole> findAllByUserIdWithRoleAndApplication(UUID userId);

    @Query("""
            select uar
            from UserApplicationRole uar
            join fetch uar.role r
            join fetch r.application a
            where uar.user.id = :userId
              and a.id = :applicationId
            """)
    List<UserApplicationRole> findAllByUserIdAndApplicationIdWithRole(UUID userId, UUID applicationId);
}
