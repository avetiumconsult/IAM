package com.aventium.identity_service.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class LoggingAuditPublisher implements AuditPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingAuditPublisher.class);

    @Override
    public void publish(String action, UUID userId, String ip, String userAgent, Map<String, ?> details) {
        log.info("AUDIT action={} userId={} ip={} ua={} details={}", action, userId, ip, userAgent, details);
    }
}
