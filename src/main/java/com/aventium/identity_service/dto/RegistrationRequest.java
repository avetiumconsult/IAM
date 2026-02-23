package com.aventium.identity_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @Size(min = 12, max = 128) String password
) {}
