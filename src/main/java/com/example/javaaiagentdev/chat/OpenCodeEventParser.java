package com.example.javaaiagentdev.chat;

import java.util.Optional;
import tools.jackson.databind.JsonNode;

/** Turns a raw OpenCode event into a normalized run event. */
public class OpenCodeEventParser {

    public Optional<RunEvent> parse(JsonNode event) {
        String type = event.path("type").asText();
        if ("session.text.delta".equals(type)) {
            JsonNode data = event.path("data");
            return Optional.of(new TextDeltaEvent(
                    data.path("sessionID").asText(),
                    data.path("assistantMessageID").asText(),
                    data.path("ordinal").asInt(),
                    data.path("delta").asText()));
        }
        return Optional.empty();
    }
}
