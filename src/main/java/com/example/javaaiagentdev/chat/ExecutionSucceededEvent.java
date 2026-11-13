package com.example.javaaiagentdev.chat;

/** Execution finished successfully; the run closes. */
public record ExecutionSucceededEvent(String sessionId) implements RunEvent {
}
