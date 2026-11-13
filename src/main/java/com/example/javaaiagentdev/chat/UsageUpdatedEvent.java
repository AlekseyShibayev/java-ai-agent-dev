package com.example.javaaiagentdev.chat;

/** Cumulative session usage. */
public record UsageUpdatedEvent(String sessionId, double cost, Usage tokens) implements RunEvent {
}
