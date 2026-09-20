package com.ai.healthbridgemcp.chat;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Natural-language chat endpoint powered by Groq.
 *
 * POST /api/chat  { "message": "...", "sessionId": "user-123" }
 * GET  /api/chat?message=...&sessionId=...
 * DELETE /api/chat/{sessionId}  — clear session history
 *
 * Each session maintains its own conversation history so follow-up
 * questions work naturally without re-stating context.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GroqChatService groqChatService;

    public ChatController(GroqChatService groqChatService) {
        this.groqChatService = groqChatService;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        String session = request.sessionId() != null ? request.sessionId() : "default";
        String reply = groqChatService.chat(request.message(), session);
        return Map.of(
                "model", "openai/gpt-oss-20b (Groq)",
                "sessionId", session,
                "response", reply != null ? reply : ""
        );
    }

    @GetMapping
    public Map<String, String> chatGet(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId) {
        String reply = groqChatService.chat(message, sessionId);
        return Map.of(
                "model", "openai/gpt-oss-20b (Groq)",
                "sessionId", sessionId,
                "response", reply != null ? reply : ""
        );
    }

    @DeleteMapping("/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        groqChatService.clearSession(sessionId);
        return Map.of("status", "cleared", "sessionId", sessionId);
    }

    public record ChatRequest(String message, String sessionId) {}
}
