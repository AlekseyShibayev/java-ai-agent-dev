package com.example.javaaiagentdev.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** Turns a raw OpenCode event into a normalized run event. */
@Component
public class OpenCodeEventParser {

    public Optional<RunEvent> parse(JsonNode event) {
        JsonNode data = event.path("data");
        String sessionId = data.path("sessionID").asText();
        return switch (event.path("type").asText()) {
            case "session.text.delta" ->
                    Optional.of(new TextDeltaEvent(sessionId, messageId(data), ordinal(data), data.path("delta").asText()));
            case "session.text.ended" ->
                    Optional.of(new TextEndedEvent(sessionId, messageId(data), ordinal(data), data.path("text").asText()));
            case "session.reasoning.delta" ->
                    Optional.of(new ReasoningDeltaEvent(sessionId, messageId(data), ordinal(data),
                            data.path("delta").asText()));
            case "session.reasoning.ended" ->
                    Optional.of(new ReasoningEndedEvent(sessionId, messageId(data), ordinal(data),
                            data.path("text").asText()));
            case "session.tool.input.started" ->
                    Optional.of(new ToolStartedEvent(sessionId, messageId(data), data.path("id").asText(),
                            data.path("name").asText()));
            case "session.tool.called" ->
                    Optional.of(new ToolCalledEvent(sessionId, messageId(data), data.path("id").asText(),
                            data.path("input")));
            case "session.tool.failed" ->
                    Optional.of(new ToolFailedEvent(sessionId, messageId(data), data.path("id").asText(),
                            data.path("error").path("type").asText(),
                            data.path("error").path("message").asText()));
            case "session.usage.updated" ->
                    Optional.of(new UsageUpdatedEvent(sessionId, data.path("cost").asDouble(),
                            usage(data.path("tokens"))));
            case "session.step.ended" ->
                    Optional.of(new StepEndedEvent(sessionId, messageId(data), data.path("finish").asText(),
                            data.path("cost").asDouble(), usage(data.path("tokens"))));
            case "session.execution.started" -> Optional.of(new ExecutionStartedEvent(sessionId));
            case "session.execution.succeeded" -> Optional.of(new ExecutionSucceededEvent(sessionId));
            case "session.execution.interrupted" ->
                    Optional.of(new ExecutionInterruptedEvent(sessionId, data.path("reason").asText()));
            case "permission.asked" ->
                    Optional.of(new PermissionAskedEvent(sessionId, data.path("id").asText(),
                            data.path("action").asText(), strings(data.path("resources")), strings(data.path("save"))));
            case "permission.replied" ->
                    Optional.of(new PermissionRepliedEvent(sessionId, data.path("requestID").asText(),
                            data.path("reply").asText()));
            default -> Optional.empty();
        };
    }

    private static String messageId(JsonNode data) {
        return data.path("assistantMessageID").asText();
    }

    private static int ordinal(JsonNode data) {
        return data.path("ordinal").asInt();
    }

    private static Usage usage(JsonNode tokens) {
        JsonNode cache = tokens.path("cache");
        return new Usage(tokens.path("input").asLong(), tokens.path("output").asLong(),
                tokens.path("reasoning").asLong(), cache.path("read").asLong(), cache.path("write").asLong());
    }

    private static List<String> strings(JsonNode array) {
        List<String> result = new ArrayList<>();
        array.forEach(node -> result.add(node.asText()));
        return List.copyOf(result);
    }
}
