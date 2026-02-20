package com.aventium.identity_service.dto;

import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        String username,
        String email,
        String status
) {}
