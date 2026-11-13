package com.example.javaaiagentdev.web;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

/** OpenCode session of the chat page, kept in the HTTP session. */
public final class ChatPageSession {

    private static final String SESSION_ID = "chat.opencodeSessionId";
    private static final String RUN_ID = "chat.runId";

    private final HttpSession httpSession;

    public ChatPageSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public String openCodeSessionId() {
        return (String) httpSession.getAttribute(SESSION_ID);
    }

    public Optional<String> runId() {
        return Optional.ofNullable((String) httpSession.getAttribute(RUN_ID));
    }

    public void store(String openCodeSessionId, String runId) {
        httpSession.setAttribute(SESSION_ID, openCodeSessionId);
        httpSession.setAttribute(RUN_ID, runId);
    }
}
