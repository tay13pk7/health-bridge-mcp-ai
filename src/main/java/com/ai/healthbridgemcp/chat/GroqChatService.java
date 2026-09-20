package com.ai.healthbridgemcp.chat;

import com.ai.healthbridgemcp.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Groq-powered chat service — uses manual tool dispatch to stay within
 * Groq free-tier TPM limits (8,000 tokens/min). Instead of sending all
 * tool schemas to Groq (which alone costs ~3,000 tokens), we parse
 * the user's intent locally, call the tools directly, then send only
 * the small data payload to Groq for human-friendly summarization.
 */
@Service
public class GroqChatService {

    private static final Logger log = LoggerFactory.getLogger(GroqChatService.class);
    private static final int MAX_HISTORY_CHARS = 800;

    private final ChatClient chatClient;
    private final AthenaTools athenaTools;
    private final ModMedTools modMedTools;
    private final NextgenTools nextgenTools;
    private final AllscriptsTools allscriptsTools;
    private final JobDashboardTools jobDashboardTools;

    // sessionId -> last AI reply (lightweight memory)
    private final Map<String, String> lastReply = new ConcurrentHashMap<>();

    private static final Pattern TENANT_PATTERN = Pattern.compile(
            "\\[Tenant:\\s*(\\S+)\\]", Pattern.CASE_INSENSITIVE);

    public GroqChatService(ChatModel chatModel,
                           AthenaTools athenaTools,
                           ModMedTools modMedTools,
                           NextgenTools nextgenTools,
                           AllscriptsTools allscriptsTools,
                           JobDashboardTools jobDashboardTools) {
        // No defaultTools — keeps token usage minimal
        this.chatClient = ChatClient.builder(chatModel).build();
        this.athenaTools = athenaTools;
        this.modMedTools = modMedTools;
        this.nextgenTools = nextgenTools;
        this.allscriptsTools = allscriptsTools;
        this.jobDashboardTools = jobDashboardTools;
    }

    public String chat(String userMessage, String sessionId) {
        log.info("CHAT | session={} | query: {}", sessionId, userMessage);

        String tenant = extractTenant(userMessage);
        String query = userMessage.replaceAll("\\[Tenant:\\s*\\S+\\]\\s*", "").trim();
        String lower = query.toLowerCase();

        // Dispatch to tools based on keywords
        Object toolData = dispatch(lower, tenant);
        String toolsUsed = toolData != null ? "yes" : "none";

        // Build a compact prompt for Groq (no tool schemas sent)
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an EHR assistant for Radix EHR Bridge. Be concise, use markdown tables/bullets.\n");
        prompt.append("User question: ").append(query).append("\n");
        prompt.append("Tenant: ").append(tenant).append("\n");

        if (toolData != null) {
            String dataStr = toolData.toString();
            // Truncate large data to stay within TPM
            if (dataStr.length() > 1500) {
                dataStr = dataStr.substring(0, 1500) + "...(truncated)";
            }
            prompt.append("EHR Data:\n").append(dataStr).append("\n");
            prompt.append("Summarize this data for the user. Flag any INACTIVE/FAILED/ERROR issues.\n");
        } else {
            // Include last reply for conversational context
            String prev = lastReply.get(sessionId);
            if (prev != null) {
                String context = prev.length() > MAX_HISTORY_CHARS
                        ? prev.substring(0, MAX_HISTORY_CHARS) + "..."
                        : prev;
                prompt.append("Previous answer: ").append(context).append("\n");
            }
            prompt.append("Answer the user's question based on your EHR knowledge. If you need specific data, tell the user which button to click.\n");
        }

        String reply;
        try {
            reply = chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("429") || msg.contains("rate_limit")) {
                log.warn("CHAT | Rate limited, returning tool data directly");
                reply = toolData != null
                        ? "**Rate limit reached — here's the raw data:**\n\n```\n" + toolData + "\n```\n\n_Wait 15 seconds and try again for an AI summary._"
                        : "**Rate limit reached.** Please wait ~15 seconds and try again.";
            } else {
                throw e;
            }
        }

        if (reply != null) {
            lastReply.put(sessionId, reply);
        }
        log.info("CHAT | session={} | tools={} | reply_length={}",
                sessionId, toolsUsed, reply != null ? reply.length() : 0);
        return reply;
    }

    public String chat(String userMessage) {
        return chat(userMessage, "default");
    }

    public void clearSession(String sessionId) {
        lastReply.remove(sessionId);
        log.info("CHAT | session={} | cleared", sessionId);
    }

    /** Summarize a raw tool result — used by ToolTestController?summarize=true */
    public String summarize(String toolName, Object rawResult) {
        String data = rawResult.toString();
        if (data.length() > 1200) {
            data = data.substring(0, 1200) + "...(truncated)";
        }
        try {
            return chatClient.prompt()
                    .user("Summarize concisely with bullets. Flag errors.\nTool: " + toolName + "\nData: " + data)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Summarize failed (likely rate limit), returning raw data");
            return "AI summary unavailable (rate limit). Raw data:\n" + rawResult;
        }
    }

    // --- Intent detection & manual tool dispatch ---

    private Object dispatch(String lower, String tenant) {
        if (matches(lower, "provider", "doctor", "physician", "staff")) {
            return dispatchProviders(lower, tenant);
        }
        if (matches(lower, "appointment", "schedule", "booking", "today")) {
            return dispatchSchedule(lower, tenant);
        }
        if (matches(lower, "patient", "search patient", "find patient")) {
            return dispatchPatients(lower, tenant);
        }
        if (matches(lower, "connection", "connect", "test connection", "status")) {
            return dispatchConnection(lower, tenant);
        }
        if (matches(lower, "sync", "sync status", "synchroniz")) {
            return dispatchSync(lower, tenant);
        }
        if (matches(lower, "failed", "fail", "error", "job")) {
            return jobDashboardTools.jobs_getFailedJobs(tenant, 24);
        }
        if (matches(lower, "department", "dept")) {
            return athenaTools.athena_getDepartments(tenant);
        }
        if (matches(lower, "location", "office", "clinic site")) {
            return modMedTools.modmed_getLocations(tenant);
        }
        if (matches(lower, "medication", "medicine", "drug", "prescription", "rx")) {
            return dispatchMedications(lower, tenant);
        }
        return null; // no tool match — Groq answers from general knowledge
    }

    private Object dispatchProviders(String lower, String tenant) {
        String ehr = detectEhr(lower, tenant);
        return switch (ehr) {
            case "modmed" -> modMedTools.modmed_getProviders(tenant);
            default -> athenaTools.athena_getProviders(tenant);
        };
    }

    private Object dispatchSchedule(String lower, String tenant) {
        String ehr = detectEhr(lower, tenant);
        return switch (ehr) {
            case "modmed" -> modMedTools.modmed_getAppointments(tenant, "");
            case "allscripts" -> allscriptsTools.allscripts_getSchedule(tenant, "");
            default -> athenaTools.athena_getAppointmentTypes(tenant);
        };
    }

    private Object dispatchPatients(String lower, String tenant) {
        String ehr = detectEhr(lower, tenant);
        return switch (ehr) {
            case "nextgen" -> nextgenTools.nextgen_getPatients(tenant);
            default -> athenaTools.athena_searchPatients(tenant, "");
        };
    }

    private Object dispatchConnection(String lower, String tenant) {
        String ehr = detectEhr(lower, tenant);
        return switch (ehr) {
            case "modmed" -> modMedTools.modmed_testConnection(tenant);
            case "nextgen" -> nextgenTools.nextgen_testConnection(tenant);
            case "allscripts" -> allscriptsTools.allscripts_testConnection(tenant);
            default -> athenaTools.athena_testConnection(tenant);
        };
    }

    private Object dispatchSync(String lower, String tenant) {
        String ehr = detectEhr(lower, tenant);
        return switch (ehr) {
            case "nextgen" -> nextgenTools.nextgen_getSyncStatus(tenant);
            default -> jobDashboardTools.jobs_getStatus(tenant, detectEhr(lower, tenant).toUpperCase());
        };
    }

    private Object dispatchMedications(String lower, String tenant) {
        // Medications data comes from Athena search patients (includes medication info)
        return athenaTools.athena_searchPatients(tenant, "");
    }

    /** Detect which EHR the user is asking about based on keywords or tenant name */
    private String detectEhr(String lower, String tenant) {
        if (lower.contains("modmed") || lower.contains("mod med"))
            return "modmed";
        if (lower.contains("nextgen") || lower.contains("next gen"))
            return "nextgen";
        if (lower.contains("allscripts") || lower.contains("all scripts"))
            return "allscripts";
        if (lower.contains("athena"))
            return "athena";

        // Detect from tenant name
        String t = tenant.toUpperCase();
        if (t.contains("DALLAS") || t.contains("PLANO"))
            return "modmed";
        if (t.contains("BAYVIEW") || t.contains("LAKESIDE"))
            return "nextgen";
        if (t.contains("METRO") || t.contains("SUMMIT"))
            return "allscripts";
        return "athena";
    }

    private String extractTenant(String message) {
        Matcher m = TENANT_PATTERN.matcher(message);
        return m.find() ? m.group(1) : "ACME-DEMO";
    }

    private boolean matches(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
