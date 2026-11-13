package com.example.javaaiagentdev.chat;

/** Token usage of a step or a session. */
public record Usage(long input, long output, long reasoning, long cacheRead, long cacheWrite) {
}
