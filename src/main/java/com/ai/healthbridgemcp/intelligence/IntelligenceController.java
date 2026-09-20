package com.ai.healthbridgemcp.intelligence;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints for AI-powered proactive intelligence.
 *
 * GET /api/monitor/{tenantId}     — proactive health scan with severity alerts
 * GET /api/report/executive       — board-ready report across all tenants & EHRs
 */
@RestController
public class IntelligenceController {

    private final EhrIntelligenceService intelligenceService;

    public IntelligenceController(EhrIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    /**
     * Proactive health monitor for a single tenant.
     * Groq analyzes ALL connections + jobs and returns severity-graded alerts.
     *
     * Try: GET /api/monitor/RIVERSIDE-HEALTH  (should surface issues)
     *      GET /api/monitor/ACME-DEMO          (should be mostly green)
     */
    @GetMapping("/api/monitor/{tenantId}")
    public Map<String, Object> healthMonitor(@PathVariable String tenantId) {
        return intelligenceService.runHealthMonitor(tenantId);
    }

    /**
     * Executive report across ALL tenants and ALL EHR systems.
     * Board-ready markdown — suitable for management presentations.
     */
    @GetMapping("/api/report/executive")
    public Map<String, Object> executiveReport() {
        return intelligenceService.generateExecutiveReport();
    }
}
