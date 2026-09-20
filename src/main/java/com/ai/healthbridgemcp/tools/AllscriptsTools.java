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
 * MCP Tools for Allscripts EHR.
 */
@Service
public class AllscriptsTools {

    private static final Logger log = LoggerFactory.getLogger(AllscriptsTools.class);
    private final AuditLogger audit;

    public AllscriptsTools(AuditLogger audit) {
        this.audit = audit;
    }

    @Tool(description = "Test connectivity to the Allscripts Unity API for a given tenant. " +
            "Returns connection status, latency, and last sync time.")
    public Map<String, Object> allscripts_testConnection(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("allscripts_testConnection", tenantId, null);
        return MockDataProvider.allscriptsConnectionTest(tenantId);
    }

    @Tool(description = "Get the provider schedule from Allscripts for a specific date. " +
            "Returns time slots showing provider, patient, visit type, and status " +
            "(COMPLETED, CHECKED_IN, CONFIRMED, AVAILABLE, NO_SHOW, SCHEDULED). " +
            "AVAILABLE slots indicate open booking opportunities. " +
            "If no date is provided, returns today's schedule.")
    public List<Map<String, Object>> allscripts_getSchedule(
            @ToolParam(description = "The tenant/practice identifier") String tenantId,
            @ToolParam(description = "Date in YYYY-MM-DD format, or empty for today") String date) {

        audit.logToolCall("allscripts_getSchedule", tenantId, "date=" + date);
        return MockDataProvider.allscriptsSchedule(tenantId, date);
    }
}

