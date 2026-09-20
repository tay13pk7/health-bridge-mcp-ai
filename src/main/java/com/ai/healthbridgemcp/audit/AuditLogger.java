package com.ai.healthbridgemcp.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Logs every MCP tool invocation for auditability.
 * In production, these would feed into ELK/Splunk for HIPAA compliance.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    public void logToolCall(String toolName, String tenantId, String parameters) {
        log.info("AUDIT | tool={} | tenant={} | timestamp={} | params={}",
                toolName,
                tenantId,
                Instant.now().toString(),
                parameters != null ? parameters : "none"
        );
    }

    public void logToolCall(String toolName, String tenantId, String parameters, String flag) {
        log.warn("AUDIT | tool={} | tenant={} | timestamp={} | flag={} | params={}",
                toolName,
                tenantId,
                Instant.now().toString(),
                flag,
                parameters != null ? parameters : "none"
        );
    }
}

