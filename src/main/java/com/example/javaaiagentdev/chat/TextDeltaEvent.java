package com.example.javaaiagentdev.chat;

/** Incremental assistant text. */
public record TextDeltaEvent(String sessionId, String messageId, int ordinal, String delta) implements RunEvent {
}
