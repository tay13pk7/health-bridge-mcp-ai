package com.ai.healthbridgemcp.controller;

import com.ai.healthbridgemcp.chat.GroqChatService;
import com.ai.healthbridgemcp.tools.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for testing MCP tools directly via Postman.
 * This is a convenience layer — in production, AI clients use the MCP protocol (SSE).
 * For demo/testing, this lets you call tools as simple REST endpoints.
 *
 * Every tool endpoint supports an optional {@code ?summarize=true} query param.
 * When set, the raw tool response is passed through Groq (llama-3.3-70b-versatile)
 * to produce a natural-language summary.
 */
@RestController
@RequestMapping("/api/test")
public class ToolTestController {

    private final AthenaTools athenaTools;
    private final ModMedTools modMedTools;
    private final NextgenTools nextgenTools;
    private final AllscriptsTools allscriptsTools;
    private final JobDashboardTools jobDashboardTools;
    private final GroqChatService groqChatService;

    public ToolTestController(AthenaTools athenaTools,
                              ModMedTools modMedTools,
                              NextgenTools nextgenTools,
                              AllscriptsTools allscriptsTools,
                              JobDashboardTools jobDashboardTools,
                              GroqChatService groqChatService) {
        this.athenaTools = athenaTools;
        this.modMedTools = modMedTools;
        this.nextgenTools = nextgenTools;
        this.allscriptsTools = allscriptsTools;
        this.jobDashboardTools = jobDashboardTools;
        this.groqChatService = groqChatService;
    }

    private Object maybeSummarize(String toolName, Object result, boolean summarize) {
        if (!summarize) {
            return result;
        }
        return Map.of(
                "tool", toolName,
                "raw", result,
                "summary", groqChatService.summarize(toolName, result)
        );
    }

    // ==================== ATHENA ====================

    @GetMapping("/athena/connection/{tenantId}")
    public Object athenaTestConnection(@PathVariable String tenantId,
                                       @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("athena_testConnection",
                athenaTools.athena_testConnection(tenantId), summarize);
    }

    @GetMapping("/athena/providers/{tenantId}")
    public Object athenaGetProviders(@PathVariable String tenantId,
                                     @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("athena_getProviders",
                athenaTools.athena_getProviders(tenantId), summarize);
    }

    @GetMapping("/athena/appointment-types/{tenantId}")
    public Object athenaGetAppointmentTypes(@PathVariable String tenantId,
                                            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("athena_getAppointmentTypes",
                athenaTools.athena_getAppointmentTypes(tenantId), summarize);
    }

    @GetMapping("/athena/departments/{tenantId}")
    public Object athenaGetDepartments(@PathVariable String tenantId,
                                       @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("athena_getDepartments",
                athenaTools.athena_getDepartments(tenantId), summarize);
    }

    @GetMapping("/athena/patients/{tenantId}")
    public Object athenaSearchPatients(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "") String searchName,
            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("athena_searchPatients",
                athenaTools.athena_searchPatients(tenantId, searchName), summarize);
    }

    // ==================== MODMED ====================

    @GetMapping("/modmed/connection/{tenantId}")
    public Object modmedTestConnection(@PathVariable String tenantId,
                                       @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("modmed_testConnection",
                modMedTools.modmed_testConnection(tenantId), summarize);
    }

    @GetMapping("/modmed/providers/{tenantId}")
    public Object modmedGetProviders(@PathVariable String tenantId,
                                     @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("modmed_getProviders",
                modMedTools.modmed_getProviders(tenantId), summarize);
    }

    @GetMapping("/modmed/appointments/{tenantId}")
    public Object modmedGetAppointments(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("modmed_getAppointments",
                modMedTools.modmed_getAppointments(tenantId, date), summarize);
    }

    @GetMapping("/modmed/locations/{tenantId}")
    public Object modmedGetLocations(@PathVariable String tenantId,
                                     @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("modmed_getLocations",
                modMedTools.modmed_getLocations(tenantId), summarize);
    }

    // ==================== NEXTGEN ====================

    @GetMapping("/nextgen/connection/{tenantId}")
    public Object nextgenTestConnection(@PathVariable String tenantId,
                                        @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("nextgen_testConnection",
                nextgenTools.nextgen_testConnection(tenantId), summarize);
    }

    @GetMapping("/nextgen/patients/{tenantId}")
    public Object nextgenGetPatients(@PathVariable String tenantId,
                                     @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("nextgen_getPatients",
                nextgenTools.nextgen_getPatients(tenantId), summarize);
    }

    @GetMapping("/nextgen/sync-status/{tenantId}")
    public Object nextgenGetSyncStatus(@PathVariable String tenantId,
                                       @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("nextgen_getSyncStatus",
                nextgenTools.nextgen_getSyncStatus(tenantId), summarize);
    }

    // ==================== ALLSCRIPTS ====================

    @GetMapping("/allscripts/connection/{tenantId}")
    public Object allscriptsTestConnection(@PathVariable String tenantId,
                                           @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("allscripts_testConnection",
                allscriptsTools.allscripts_testConnection(tenantId), summarize);
    }

    @GetMapping("/allscripts/schedule/{tenantId}")
    public Object allscriptsGetSchedule(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("allscripts_getSchedule",
                allscriptsTools.allscripts_getSchedule(tenantId, date), summarize);
    }

    // ==================== JOB DASHBOARD ====================

    @GetMapping("/jobs/status/{tenantId}")
    public Object jobsGetStatus(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "") String ehrType,
            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("jobs_getStatus",
                jobDashboardTools.jobs_getStatus(tenantId, ehrType), summarize);
    }

    @GetMapping("/jobs/failed/{tenantId}")
    public Object jobsGetFailedJobs(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "false") boolean summarize) {
        return maybeSummarize("jobs_getFailedJobs",
                jobDashboardTools.jobs_getFailedJobs(tenantId, hours), summarize);
    }

    // ==================== LIST ALL TOOLS ====================

    @GetMapping("/tools")
    public Map<String, Object> listAllTools() {
        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("totalTools", 16);
        tools.put("chat", List.of(
            Map.of("name", "groq_chat", "testUrl", "POST /api/chat  { \"message\": \"...\" }")
        ));
        tools.put("athena", List.of(
            Map.of("name", "athena_testConnection", "testUrl", "/api/test/athena/connection/ACME-DEMO"),
            Map.of("name", "athena_getProviders", "testUrl", "/api/test/athena/providers/ACME-DEMO"),
            Map.of("name", "athena_getAppointmentTypes", "testUrl", "/api/test/athena/appointment-types/ACME-DEMO"),
            Map.of("name", "athena_getDepartments", "testUrl", "/api/test/athena/departments/ACME-DEMO"),
            Map.of("name", "athena_searchPatients", "testUrl", "/api/test/athena/patients/ACME-DEMO?searchName=Williams")
        ));
        tools.put("modmed", List.of(
            Map.of("name", "modmed_testConnection", "testUrl", "/api/test/modmed/connection/ACME-DEMO"),
            Map.of("name", "modmed_getProviders", "testUrl", "/api/test/modmed/providers/ACME-DEMO"),
            Map.of("name", "modmed_getAppointments", "testUrl", "/api/test/modmed/appointments/ACME-DEMO?date=2026-08-12"),
            Map.of("name", "modmed_getLocations", "testUrl", "/api/test/modmed/locations/ACME-DEMO")
        ));
        tools.put("nextgen", List.of(
            Map.of("name", "nextgen_testConnection", "testUrl", "/api/test/nextgen/connection/BAYVIEW-CLINIC"),
            Map.of("name", "nextgen_getPatients", "testUrl", "/api/test/nextgen/patients/BAYVIEW-CLINIC"),
            Map.of("name", "nextgen_getSyncStatus", "testUrl", "/api/test/nextgen/sync-status/LAKESIDE-HEALTH")
        ));
        tools.put("allscripts", List.of(
            Map.of("name", "allscripts_testConnection", "testUrl", "/api/test/allscripts/connection/ACME-DEMO"),
            Map.of("name", "allscripts_getSchedule", "testUrl", "/api/test/allscripts/schedule/ACME-DEMO?date=2026-08-12")
        ));
        tools.put("jobDashboard", List.of(
            Map.of("name", "jobs_getStatus", "testUrl", "/api/test/jobs/status/ACME-DEMO?ehrType=ATHENA"),
            Map.of("name", "jobs_getFailedJobs", "testUrl", "/api/test/jobs/failed/ACME-DEMO?hours=24")
        ));
        tools.put("hint", "Append ?summarize=true to any endpoint to get a Groq-generated natural-language summary.");
        return tools;
    }
}

