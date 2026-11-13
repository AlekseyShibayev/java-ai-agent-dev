package com.example.javaaiagentdev.chat;

/** Tool call failed; the run keeps going. */
public record ToolFailedEvent(String sessionId, String messageId, String callId, String errorType, String message)
        implements RunEvent {
}
