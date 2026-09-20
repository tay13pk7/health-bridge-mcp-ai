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
 * MCP Tools for cross-vendor Job Execution Dashboard.
 * Monitors sync jobs across all connected EHRs.
 */
@Service
public class JobDashboardTools {

    private static final Logger log = LoggerFactory.getLogger(JobDashboardTools.class);
    private final AuditLogger audit;

    public JobDashboardTools(AuditLogger audit) {
        this.audit = audit;
    }

    @Tool(description = "Get a summary of EHR sync job statuses for a tenant. " +
            "Shows job counts (total, successful, failed, running) for the last 24 hours and 7 days. " +
            "Also shows when each EHR was last successfully synced. " +
            "Optionally filter by a specific EHR type (ATHENA, MODMED, NEXTGEN, ALLSCRIPTS) " +
            "or pass empty/null to see all EHRs.")
    public Map<String, Object> jobs_getStatus(
            @ToolParam(description = "The tenant/practice identifier") String tenantId,
            @ToolParam(description = "EHR type filter: ATHENA, MODMED, NEXTGEN, ALLSCRIPTS — or empty for all") String ehrType) {

        audit.logToolCall("jobs_getStatus", tenantId, "ehrType=" + ehrType);
        return MockDataProvider.jobStatusSummary(tenantId, ehrType);
    }

    @Tool(description = "Get a list of failed EHR sync jobs in the last N hours. " +
            "Returns job name, EHR type, start/end time, error message, records processed, and retry count. " +
            "Useful for troubleshooting integration issues. Default lookback is 24 hours.")
    public List<Map<String, Object>> jobs_getFailedJobs(
            @ToolParam(description = "The tenant/practice identifier") String tenantId,
            @ToolParam(description = "Number of hours to look back, e.g. 24") int hours) {

        audit.logToolCall("jobs_getFailedJobs", tenantId, "hours=" + hours);
        return MockDataProvider.failedJobs(tenantId, hours);
    }
}

