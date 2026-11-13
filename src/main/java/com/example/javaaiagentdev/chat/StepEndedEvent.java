package com.example.javaaiagentdev.chat;

/** Per-step usage. */
public record StepEndedEvent(String sessionId, String messageId, String finish, double cost, Usage tokens)
        implements RunEvent {
}
