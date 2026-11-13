package com.example.javaaiagentdev.chat;

/** Execution interrupted; the run closes as interrupted. */
public record ExecutionInterruptedEvent(String sessionId, String reason) implements RunEvent {
}
