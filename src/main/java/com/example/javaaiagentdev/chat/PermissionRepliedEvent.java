package com.example.javaaiagentdev.chat;

/** The permission request was answered. */
public record PermissionRepliedEvent(String sessionId, String requestId, String reply) implements RunEvent {
}
