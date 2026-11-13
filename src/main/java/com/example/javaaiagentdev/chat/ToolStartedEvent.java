package com.example.javaaiagentdev.chat;

/** Tool call declaration: the model has started filling the call arguments. */
public record ToolStartedEvent(String sessionId, String messageId, String callId, String name) implements RunEvent {
}
