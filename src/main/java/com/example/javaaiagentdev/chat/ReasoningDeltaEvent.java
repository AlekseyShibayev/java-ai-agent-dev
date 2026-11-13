package com.example.javaaiagentdev.chat;

/** Incremental reasoning text. */
public record ReasoningDeltaEvent(String sessionId, String messageId, int ordinal, String delta) implements RunEvent {
}
