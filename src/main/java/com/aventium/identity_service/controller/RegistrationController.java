package com.aventium.identity_service.controller;

import com.aventium.identity_service.dto.RegistrationRequest;
import com.aventium.identity_service.dto.RegistrationResponse;
import com.aventium.identity_service.entity.User;
import com.aventium.identity_service.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", 
               description = "Creates a new user account and generates an email verification token. Check application logs for the verification token to test email verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully. Email verification token is logged.",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.register(request);
        return new RegistrationResponse(user.getId(), user.getUsername(), user.getEmail(), "PENDING_VERIFICATION");
    }

}
