package com.example.javaaiagentdev.chat;

import tools.jackson.databind.JsonNode;

/** Tool call with parsed arguments. The result comes from the session messages, not from events. */
public record ToolCalledEvent(String sessionId, String messageId, String callId, JsonNode input) implements RunEvent {
}
