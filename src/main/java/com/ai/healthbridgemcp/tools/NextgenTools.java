package com.ai.healthbridgemcp.tools;

import com.ai.healthbridgemcp.audit.AuditLogger;
import com.ai.healthbridgemcp.mock.MockDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Tools for Nextgen EHR (formerly known as Healthjump in this codebase).
 * Tenants: BAYVIEW-CLINIC (healthy) | LAKESIDE-HEALTH (degraded)
 */
@Service
public class NextgenTools {

    private static final Logger log = LoggerFactory.getLogger(NextgenTools.class);
    private final AuditLogger audit;

    public NextgenTools(AuditLogger audit) {
        this.audit = audit;
    }

    @Tool(description = "Test connectivity to the Nextgen EHR API for a given tenant. " +
            "Returns connection status, latency, token validity, and last sync time. " +
            "Known tenants: BAYVIEW-CLINIC (healthy), LAKESIDE-HEALTH (has connectivity issues).")
    public Map<String, Object> nextgen_testConnection(
            @ToolParam(description = "The tenant/practice identifier, e.g. 'BAYVIEW-CLINIC' or 'LAKESIDE-HEALTH'") String tenantId) {

        audit.logToolCall("nextgen_testConnection", tenantId, null);
        return MockDataProvider.healthjumpConnectionTest(tenantId);
    }

    @Tool(description = "Get patients synced from Nextgen EHR for a tenant. " +
            "Returns patient demographics with PHI fields automatically redacted. " +
            "Includes insurance plan, primary provider, last visit date, and active status.")
    public List<Map<String, Object>> nextgen_getPatients(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("nextgen_getPatients", tenantId, null);
        return MockDataProvider.healthjumpPatients(tenantId);
    }

    @Tool(description = "Get the current data sync status for a Nextgen EHR tenant. " +
            "Shows last full sync, last incremental sync, records synced by category " +
            "(patients, appointments, providers, insurance plans), error count, and next scheduled sync. " +
            "Useful for monitoring integration health. LAKESIDE-HEALTH is known to have sync failures.")
    public Map<String, Object> nextgen_getSyncStatus(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("nextgen_getSyncStatus", tenantId, null);
        return MockDataProvider.healthjumpSyncStatus(tenantId);
    }
}
