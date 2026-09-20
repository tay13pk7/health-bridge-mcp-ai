package com.ai.healthbridgemcp.intelligence;

import com.ai.healthbridgemcp.mock.MockDataProvider;
import com.ai.healthbridgemcp.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI-powered intelligence layer using Groq.
 *
 * Two capabilities:
 *  1. Proactive Health Monitor — scans all EHR connections + jobs for a tenant
 *     and returns severity-graded alerts WITH recommendations.
 *  2. Executive Report — one-click board-ready report covering all EHR systems.
 */
@Service
public class EhrIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(EhrIntelligenceService.class);

    private final ChatClient chatClient;
    private final AthenaTools athenaTools;
    private final ModMedTools modMedTools;
    private final NextgenTools nextgenTools;
    private final AllscriptsTools allscriptsTools;
    private final JobDashboardTools jobDashboardTools;

    public EhrIntelligenceService(ChatModel chatModel,
                                  AthenaTools athenaTools,
                                  ModMedTools modMedTools,
                                  NextgenTools nextgenTools,
                                  AllscriptsTools allscriptsTools,
                                  JobDashboardTools jobDashboardTools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.athenaTools = athenaTools;
        this.modMedTools = modMedTools;
        this.nextgenTools = nextgenTools;
        this.allscriptsTools = allscriptsTools;
        this.jobDashboardTools = jobDashboardTools;
    }

    /**
     * Proactive Health Monitor: collects raw data from all relevant EHR systems
     * for the given tenant, then asks Groq to analyze and return structured alerts.
     */
    public Map<String, Object> runHealthMonitor(String tenantId) {
        log.info("MONITOR | starting health scan for tenant={}", tenantId);
        long start = System.currentTimeMillis();

        String ehr = MockDataProvider.deriveEhr(tenantId);
        Map<String, Object> rawData = collectTenantData(tenantId, ehr);

        String dataStr = rawData.toString();
        if (dataStr.length() > 1500) {
            dataStr = dataStr.substring(0, 1500) + "...(truncated)";
        }

        String prompt = "Analyze EHR health data for tenant " + tenantId + ". "
                + "Reply with: Overall Status (HEALTHY/WARNING/CRITICAL), Alerts with severity, "
                + "Root Causes, Recommended Actions. Use markdown.\n\nData:\n" + dataStr;

        String analysis;
        try {
            analysis = chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("MONITOR | AI analysis failed for tenant={}, using raw data", tenantId);
            analysis = buildFallbackAnalysis(tenantId, rawData);
        }
        long elapsed = System.currentTimeMillis() - start;

        log.info("MONITOR | tenant={} | completed in {}ms", tenantId, elapsed);

        return Map.of(
                "tenant", tenantId,
                "ehr", ehr,
                "scanDurationMs", elapsed,
                "analysis", analysis != null ? analysis : "Analysis unavailable"
        );
    }

    /**
     * Executive Report: collects data from ALL tenants across ALL EHRs,
     * then asks Groq to generate a board-ready markdown report.
     */
    public Map<String, Object> generateExecutiveReport() {
        log.info("REPORT | generating executive report across all tenants");
        long start = System.currentTimeMillis();

        Map<String, List<String>> allTenants = MockDataProvider.allTenants();
        Map<String, Object> allData = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : allTenants.entrySet()) {
            String ehr = entry.getKey();
            for (String tenant : entry.getValue()) {
                allData.put(ehr + "/" + tenant, collectTenantData(tenant, ehr));
            }
        }

        // Compress to fit within TPM: just connection status + job status per tenant
        StringBuilder compact = new StringBuilder();
        for (Map.Entry<String, Object> e : allData.entrySet()) {
            compact.append(e.getKey()).append(": ");
            String val = e.getValue().toString();
            if (val.length() > 200) val = val.substring(0, 200) + "...";
            compact.append(val).append("\n");
        }

        String prompt = "Generate a brief executive EHR status report in markdown. "
                + "Include a health scorecard table per tenant (HEALTHY/WARNING/CRITICAL). "
                + "Highlight problems. End with 2-3 recommendations.\n\nData:\n" + compact;

        String report;
        try {
            report = chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("REPORT | AI report failed, using fallback");
            report = buildFallbackReport(allData);
        }
        long elapsed = System.currentTimeMillis() - start;

        log.info("REPORT | completed in {}ms", elapsed);

        return Map.of(
                "generatedAt", java.time.LocalDateTime.now().toString(),
                "tenantsScanned", allTenants.values().stream().mapToLong(List::size).sum(),
                "generationMs", elapsed,
                "report", (report != null && !report.isEmpty()) ? report : buildFallbackReport(allData)
        );
    }

    /** Collects connection, provider, and job data for a single tenant. */
    private Map<String, Object> collectTenantData(String tenantId, String ehr) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            switch (ehr) {
                case "ATHENA" -> {
                    data.put("connection", athenaTools.athena_testConnection(tenantId));
                    data.put("providers", athenaTools.athena_getProviders(tenantId));
                    data.put("departments", athenaTools.athena_getDepartments(tenantId));
                }
                case "MODMED" -> {
                    data.put("connection", modMedTools.modmed_testConnection(tenantId));
                    data.put("providers", modMedTools.modmed_getProviders(tenantId));
                    data.put("locations", modMedTools.modmed_getLocations(tenantId));
                }
                case "NEXTGEN" -> {
                    data.put("connection", nextgenTools.nextgen_testConnection(tenantId));
                    data.put("syncStatus", nextgenTools.nextgen_getSyncStatus(tenantId));
                    data.put("patients", nextgenTools.nextgen_getPatients(tenantId));
                }
                case "ALLSCRIPTS" -> {
                    data.put("connection", allscriptsTools.allscripts_testConnection(tenantId));
                    data.put("schedule", allscriptsTools.allscripts_getSchedule(tenantId, ""));
                }
            }
            data.put("failedJobs", jobDashboardTools.jobs_getFailedJobs(tenantId, 24));
            data.put("jobSummary", jobDashboardTools.jobs_getStatus(tenantId, ehr));
        } catch (Exception e) {
            log.error("MONITOR | error collecting data for tenant={}: {}", tenantId, e.getMessage());
            data.put("error", e.getMessage());
        }
        return data;
    }

    /** Fallback when Groq is rate-limited — produce a simple analysis from raw data */
    private String buildFallbackAnalysis(String tenantId, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Health Scan: ").append(tenantId).append("\n\n");
        sb.append("_AI summary unavailable (rate limit). Raw findings:_\n\n");
        for (Map.Entry<String, Object> e : data.entrySet()) {
            sb.append("**").append(e.getKey()).append(":** ").append(e.getValue()).append("\n\n");
        }
        return sb.toString();
    }

    /** Fallback when Groq is rate-limited — produce a simple report from raw data */
    private String buildFallbackReport(Map<String, Object> allData) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Executive EHR Status Report\n\n");
        sb.append("_AI summary unavailable (rate limit). Raw data per tenant:_\n\n");
        sb.append("| Tenant | Data Summary |\n|---|---|\n");
        for (Map.Entry<String, Object> e : allData.entrySet()) {
            String val = e.getValue().toString();
            if (val.length() > 150) val = val.substring(0, 150) + "...";
            sb.append("| ").append(e.getKey()).append(" | ").append(val).append(" |\n");
        }
        return sb.toString();
    }
}
