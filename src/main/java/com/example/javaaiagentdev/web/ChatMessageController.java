package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.ChatService;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.chat.RunRegistry.RunStatus;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Starts a chat run for the message of the chat page. */
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    private final ChatService service;
    private final OpenCodeClient client;
    private final RunRegistry registry;

    public ChatMessageController(ChatService service, OpenCodeClient client, RunRegistry registry) {
        this.service = service;
        this.client = client;
        this.registry = registry;
    }

    @PostMapping("/messages")
    public ResponseEntity<Object> send(@RequestBody SendMessageRequest request, HttpSession httpSession) {
        String text = request.text() == null ? "" : request.text().strip();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Message text must not be blank");
        }
        ChatPageSession page = new ChatPageSession(httpSession);
        if (page.runId().flatMap(registry::find).filter(run -> run.status() == RunStatus.ACTIVE).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Run already active"));
        }
        String sessionId = page.openCodeSessionId();
        if (sessionId == null) {
            sessionId = client.createSession("chat");
        }
        String runId = service.start(sessionId, text, request.agent());
        page.store(sessionId, runId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new StartRunResponse(runId, sessionId));
    }

    public record SendMessageRequest(String text, String agent) {
    }

    public record StartRunResponse(String runId, String sessionId) {
    }
}
