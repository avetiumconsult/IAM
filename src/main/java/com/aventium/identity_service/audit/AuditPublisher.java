package com.aventium.identity_service.audit;

import java.util.Map;
import java.util.UUID;

public interface AuditPublisher {
    void publish(String action, UUID userId, String ip, String userAgent, Map<String, ?> details);
}
