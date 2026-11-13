package com.example.javaaiagentdev.chat;

import java.util.Optional;
import tools.jackson.databind.JsonNode;

/** Turns a raw OpenCode event into a normalized run event. */
public class OpenCodeEventParser {

    public Optional<RunEvent> parse(JsonNode event) {
        return Optional.empty();
    }
}
