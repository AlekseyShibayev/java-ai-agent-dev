package com.example.javaaiagentdev.chat;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/** Extracts tool parts with their results from the session messages. */
public final class ToolResults {

    private ToolResults() {
    }

    public static List<ToolResultEvent> parse(String sessionId, JsonNode messages) {
        List<ToolResultEvent> results = new ArrayList<>();
        for (JsonNode message : messages) {
            if (!"assistant".equals(message.path("type").asText())) {
                continue;
            }
            for (JsonNode part : message.path("content")) {
                if ("tool".equals(part.path("type").asText())) {
                    JsonNode state = part.path("state");
                    results.add(new ToolResultEvent(sessionId, part.path("id").asText(),
                            state.path("status").asText(), text(state.path("content"))));
                }
            }
        }
        return results;
    }

    private static String text(JsonNode content) {
        StringBuilder result = new StringBuilder();
        for (JsonNode part : content) {
            if ("text".equals(part.path("type").asText())) {
                result.append(part.path("text").asText());
            }
        }
        return result.toString();
    }
}
