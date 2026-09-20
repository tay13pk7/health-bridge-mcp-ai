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
 * MCP Tools for ModMed (Modernizing Medicine) EHR.
 */
@Service
public class ModMedTools {

    private static final Logger log = LoggerFactory.getLogger(ModMedTools.class);
    private final AuditLogger audit;

    public ModMedTools(AuditLogger audit) {
        this.audit = audit;
    }

    @Tool(description = "Test connectivity to the ModMed EHR API for a given tenant. " +
            "Returns connection status, latency, and last sync time.")
    public Map<String, Object> modmed_testConnection(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("modmed_testConnection", tenantId, null);
        return MockDataProvider.modmedConnectionTest(tenantId);
    }

    @Tool(description = "Get all providers (doctors) configured in ModMed for a tenant. " +
            "Returns provider name, NPI, specialty, location, and status.")
    public List<Map<String, Object>> modmed_getProviders(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("modmed_getProviders", tenantId, null);
        return MockDataProvider.modmedProviders(tenantId);
    }

    @Tool(description = "Get appointments from ModMed for a tenant on a specific date. " +
            "Returns appointment details including patient, provider, time, type, status, and reason. " +
            "Shows CONFIRMED, CHECKED_IN, NO_SHOW, CANCELLED, and SCHEDULED statuses. " +
            "If no date is provided, returns today's appointments.")
    public List<Map<String, Object>> modmed_getAppointments(
            @ToolParam(description = "The tenant/practice identifier") String tenantId,
            @ToolParam(description = "Date in YYYY-MM-DD format, or empty for today") String date) {

        audit.logToolCall("modmed_getAppointments", tenantId, "date=" + date);
        return MockDataProvider.modmedAppointments(tenantId, date);
    }

    @Tool(description = "Get all practice locations configured in ModMed for a tenant. " +
            "Returns location name, address, city, state, phone, provider count, and active status.")
    public List<Map<String, Object>> modmed_getLocations(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("modmed_getLocations", tenantId, null);
        return MockDataProvider.modmedLocations(tenantId);
    }
}

