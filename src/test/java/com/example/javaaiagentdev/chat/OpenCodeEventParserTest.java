package com.example.javaaiagentdev.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void shouldParseTextFixture() throws Exception {
        List<RunEvent> events = parseAll("text.events.json");

        assertEquals(List.of(
                ExecutionStartedEvent.class,
                UsageUpdatedEvent.class,
                TextDeltaEvent.class,
                TextEndedEvent.class,
                StepEndedEvent.class,
                UsageUpdatedEvent.class,
                ExecutionSucceededEvent.class), types(events));
        assertEquals(new TextEndedEvent("ses_test", "msg_test", 0, "ok"), first(events, TextEndedEvent.class));
        StepEndedEvent step = first(events, StepEndedEvent.class);
        assertEquals(0.0008723759999999999, step.cost());
        assertEquals(new Usage(5768, 3, 0, 1792, 0), step.tokens());
        assertEquals("ses_test", first(events, ExecutionSucceededEvent.class).sessionId());
    }

    @Test
    void shouldParseToolFixture() throws Exception {
        List<RunEvent> events = parseAll("tool.events.json");

        assertEquals(List.of(
                ExecutionStartedEvent.class,
                UsageUpdatedEvent.class,
                ToolStartedEvent.class,
                ToolCalledEvent.class,
                StepEndedEvent.class,
                UsageUpdatedEvent.class,
                TextDeltaEvent.class,
                TextEndedEvent.class,
                StepEndedEvent.class,
                UsageUpdatedEvent.class,
                ExecutionSucceededEvent.class), types(events));
        assertEquals("read", first(events, ToolStartedEvent.class).name());
        assertEquals("call_test", first(events, ToolCalledEvent.class).callId());
        assertEquals("/repo/README.md", first(events, ToolCalledEvent.class).input().path("path").asText());
        assertEquals("tool-calls", first(events, StepEndedEvent.class).finish());
    }

    @Test
    void shouldParseApprovalFixture() throws Exception {
        List<RunEvent> events = parseAll("approval.events.json");

        assertEquals(List.of(
                ExecutionStartedEvent.class,
                UsageUpdatedEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningDeltaEvent.class,
                ToolStartedEvent.class,
                ReasoningEndedEvent.class,
                ToolCalledEvent.class,
                PermissionAskedEvent.class,
                PermissionRepliedEvent.class,
                ToolFailedEvent.class,
                UsageUpdatedEvent.class,
                ExecutionInterruptedEvent.class), types(events));
        PermissionAskedEvent asked = first(events, PermissionAskedEvent.class);
        assertEquals("shell", asked.action());
        assertEquals("per_test", asked.requestId());
        assertEquals(List.of("echo opencode-probe"), asked.resources());
        assertEquals(List.of("echo *"), asked.save());
        assertEquals("reject", first(events, PermissionRepliedEvent.class).reply());
        assertEquals("aborted", first(events, ToolFailedEvent.class).errorType());
        assertEquals("shutdown", first(events, ExecutionInterruptedEvent.class).reason());
    }

    @Test
    void shouldParseAbortFixture() throws Exception {
        List<RunEvent> events = parseAll("abort.events.json");

        assertEquals(List.of(
                ExecutionStartedEvent.class,
                UsageUpdatedEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningDeltaEvent.class,
                ReasoningEndedEvent.class,
                ExecutionInterruptedEvent.class), types(events));
        assertEquals("user", first(events, ExecutionInterruptedEvent.class).reason());
    }

    private List<RunEvent> parseAll(String fixture) throws Exception {
        List<RunEvent> events = new ArrayList<>();
        for (JsonNode event : readEvents(fixture)) {
            parser.parse(event).ifPresent(events::add);
        }
        return events;
    }

    private JsonNode firstEventOfType(String fixture, String type) throws Exception {
        for (JsonNode event : readEvents(fixture)) {
            if (type.equals(event.path("type").asText())) {
                return event;
            }
        }
        throw new AssertionError("Fixture has no event of type " + type);
    }

    private List<JsonNode> readEvents(String fixture) throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/opencode/" + fixture)) {
            List<JsonNode> events = new ArrayList<>();
            objectMapper.readTree(input).forEach(events::add);
            return events;
        }
    }

    private static List<Class<? extends RunEvent>> types(List<RunEvent> events) {
        return events.stream().map(RunEvent::getClass).toList();
    }

    private static <T extends RunEvent> T first(List<RunEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).findFirst()
                .orElseThrow(() -> new AssertionError("No " + type.getSimpleName() + " in " + types(events)));
    }
}
