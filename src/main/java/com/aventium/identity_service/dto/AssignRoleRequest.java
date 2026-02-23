package com.aventium.identity_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Application name is required")
    private String applicationName;

    @NotBlank(message = "Role name is required")
    private String roleName;
}
