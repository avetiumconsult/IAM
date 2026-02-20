package com.aventium.identity_service.controller;

import com.aventium.identity_service.dto.AssignRoleRequest;
import com.aventium.identity_service.entity.UserApplicationRole;
import com.aventium.identity_service.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrative endpoints for user role management. Requires ADMIN role for the target application.")
public class AdminController {

    private final UserRoleService userRoleService;

    public AdminController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PostMapping("/users/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign role to user", 
               description = "Assigns a role to a user for a specific application. Requires ADMIN role for the target application. User can then access that application with the assigned permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already has this role"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role for the target application"),
            @ApiResponse(responseCode = "404", description = "User, application, or role not found")
    })
    public UserApplicationRole assignRole(@Valid @RequestBody AssignRoleRequest request, HttpServletRequest httpRequest) {
        return userRoleService.assignRoleToUser(
                request.getUserId(),
                request.getApplicationName(),
                request.getRoleName(),
                httpRequest
        );
    }

    @DeleteMapping("/users/{userId}/applications/{applicationName}/roles/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove role from user",
               description = "Removes a role assignment from a user for a specific application. Requires ADMIN role for the target application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role removed successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role for the target application"),
            @ApiResponse(responseCode = "404", description = "User, application, role, or assignment not found")
    })
    public void removeRole(
            @PathVariable UUID userId,
            @PathVariable String applicationName,
            @PathVariable String roleName,
            HttpServletRequest httpRequest
    ) {
        userRoleService.removeRoleFromUser(userId, applicationName, roleName, httpRequest);
    }
}
