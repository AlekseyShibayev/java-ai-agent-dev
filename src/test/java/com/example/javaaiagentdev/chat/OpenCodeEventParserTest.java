package com.example.javaaiagentdev.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class OpenCodeEventParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenCodeEventParser parser = new OpenCodeEventParser();

    @Test
    void shouldParseTextDelta() throws Exception {
        JsonNode event = firstEventOfType("text.events.json", "session.text.delta");

        Optional<RunEvent> parsed = parser.parse(event);

        assertTrue(parsed.isPresent(), "text delta must produce an event");
        assertEquals(new TextDeltaEvent("ses_test", "msg_test", 0, "ok"), parsed.get());
    }

    private JsonNode firstEventOfType(String fixture, String type) throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/opencode/" + fixture)) {
            for (JsonNode event : objectMapper.readTree(input)) {
                if (type.equals(event.path("type").asText())) {
                    return event;
                }
            }
        }
        throw new AssertionError("Fixture has no event of type " + type);
    }
}
