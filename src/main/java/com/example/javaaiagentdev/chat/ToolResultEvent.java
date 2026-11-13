package com.example.javaaiagentdev.chat;

/** Tool result read from the assistant message, not from the event stream. */
public record ToolResultEvent(String sessionId, String callId, String status, String content) implements RunEvent {
}
