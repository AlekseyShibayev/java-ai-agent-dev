package com.example.javaaiagentdev.chat;

/** Final reasoning text of a step. */
public record ReasoningEndedEvent(String sessionId, String messageId, int ordinal, String text) implements RunEvent {
}
