package com.example.javaaiagentdev.chat;

import java.util.List;

/** A tool call waits for the user's decision. */
public record PermissionAskedEvent(String sessionId, String requestId, String action, List<String> resources,
        List<String> save) implements RunEvent {
}
