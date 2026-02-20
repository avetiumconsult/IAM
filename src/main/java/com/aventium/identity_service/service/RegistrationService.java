package com.aventium.identity_service.service;

import com.aventium.identity_service.dto.RegistrationRequest;
import com.aventium.identity_service.entity.User;

public interface RegistrationService {
    User register(RegistrationRequest request);
}
