package com.example.javaaiagentdev.chat;

import com.example.javaaiagentdev.opencode.OpenCodeClient;
import com.example.javaaiagentdev.opencode.OpenCodeUnavailableException;
import org.springframework.stereotype.Service;

/** Starts, cancels and answers runs of a chat session. */
@Service
public class ChatService {

    private final OpenCodeClient client;
    private final RunRegistry registry;
    private final OpenCodeEventStream eventStream;

    public ChatService(OpenCodeClient client, RunRegistry registry, OpenCodeEventStream eventStream) {
        this.client = client;
        this.registry = registry;
        this.eventStream = eventStream;
    }

    public String start(String sessionId, String text, String agent) {
        eventStream.ensureStarted();
        String runId = registry.start(sessionId);
        try {
            client.prompt(sessionId, text, agent);
        } catch (OpenCodeUnavailableException e) {
            registry.fail(runId, e.getMessage());
            throw e;
        }
        return runId;
    }

    public void abort(String sessionId) {
        client.abort(sessionId);
    }

    public void replyPermission(String sessionId, String requestId, String decision) {
        client.replyPermission(sessionId, requestId, decision);
    }
}
