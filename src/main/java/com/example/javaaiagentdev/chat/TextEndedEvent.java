package com.example.javaaiagentdev.chat;

/** Final text of an assistant text part. */
public record TextEndedEvent(String sessionId, String messageId, int ordinal, String text) implements RunEvent {
}
