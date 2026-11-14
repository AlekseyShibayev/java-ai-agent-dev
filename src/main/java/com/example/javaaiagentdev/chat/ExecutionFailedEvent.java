package com.example.javaaiagentdev.chat;

/** Execution failed with a provider error; the run closes as failed. */
public record ExecutionFailedEvent(String sessionId, String errorType, String message) implements RunEvent {
}
