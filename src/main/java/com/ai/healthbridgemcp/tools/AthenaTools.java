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
 * MCP Tools for Athena EHR.
 * Each @Tool method is auto-registered as an MCP tool that AI clients can invoke.
 */
@Service
public class AthenaTools {

    private static final Logger log = LoggerFactory.getLogger(AthenaTools.class);
    private final AuditLogger audit;

    public AthenaTools(AuditLogger audit) {
        this.audit = audit;
    }

    @Tool(description = "Test connectivity to the Athena EHR API for a given tenant. " +
            "Returns connection status, latency, and last sync time. " +
            "Use this to verify if Athena integration is working before running other operations.")
    public Map<String, Object> athena_testConnection(
            @ToolParam(description = "The tenant/practice identifier, e.g. 'ACME-DEMO'") String tenantId) {

        audit.logToolCall("athena_testConnection", tenantId, null);
        return MockDataProvider.athenaConnectionTest(tenantId);
    }

    @Tool(description = "Get the list of all providers (doctors) configured in Athena for a tenant. " +
            "Returns provider name, NPI number, specialty, department, and active status. " +
            "Useful for checking which doctors are onboarded in the system.")
    public List<Map<String, Object>> athena_getProviders(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("athena_getProviders", tenantId, null);
        return MockDataProvider.athenaProviders(tenantId);
    }

    @Tool(description = "Get all appointment types configured in Athena for a tenant. " +
            "Returns type name, duration in minutes, and category (OFFICE_VISIT, TELEHEALTH, LAB, etc.). " +
            "Useful for understanding what kinds of appointments can be scheduled.")
    public List<Map<String, Object>> athena_getAppointmentTypes(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("athena_getAppointmentTypes", tenantId, null);
        return MockDataProvider.athenaAppointmentTypes(tenantId);
    }

    @Tool(description = "Get all departments/locations configured in Athena for a tenant. " +
            "Returns department name, address, city, state, phone, and status. " +
            "Useful for seeing where the practice operates.")
    public List<Map<String, Object>> athena_getDepartments(
            @ToolParam(description = "The tenant/practice identifier") String tenantId) {

        audit.logToolCall("athena_getDepartments", tenantId, null);
        return MockDataProvider.athenaDepartments(tenantId);
    }

    @Tool(description = "Search for patients in Athena by name. " +
            "Returns patient ID, name, insurance, primary provider, and last visit date. " +
            "PHI fields like SSN and date of birth are automatically redacted. " +
            "Pass an empty string for searchName to get all patients.")
    public List<Map<String, Object>> athena_searchPatients(
            @ToolParam(description = "The tenant/practice identifier") String tenantId,
            @ToolParam(description = "Patient name to search for (first or last name), or empty for all") String searchName) {

        audit.logToolCall("athena_searchPatients", tenantId, "search=" + searchName);
        return MockDataProvider.athenaPatients(tenantId, searchName);
    }
}

