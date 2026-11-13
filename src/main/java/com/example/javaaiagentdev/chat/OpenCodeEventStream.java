package com.example.javaaiagentdev.chat;

import com.example.javaaiagentdev.opencode.OpenCodeClient;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Single reader of the shared OpenCode event stream, started on the first run. */
@Component
public class OpenCodeEventStream {

    private static final Logger log = LoggerFactory.getLogger(OpenCodeEventStream.class);

    private final OpenCodeClient client;
    private final OpenCodeEventParser parser;
    private final RunRegistry registry;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean started = new AtomicBoolean();

    public OpenCodeEventStream(OpenCodeClient client, OpenCodeEventParser parser, RunRegistry registry,
            ObjectMapper objectMapper) {
        this.client = client;
        this.parser = parser;
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public void ensureStarted() {
        if (started.compareAndSet(false, true)) {
            Thread.ofVirtual().name("opencode-events").start(this::loop);
        }
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try (Stream<String> lines = client.eventLines()) {
                SseFrames frames = new SseFrames();
                lines.forEach(line -> frames.accept(line, this::handle));
            } catch (RuntimeException e) {
                log.warn("OpenCode event stream lost: {}", e.getMessage());
                sleep();
            }
        }
    }

    private void handle(String frame) {
        try {
            JsonNode event = objectMapper.readTree(frame);
            JsonNode data = event.path("data");
            if (data.path("sessionID").asText().isBlank()) {
                return;
            }
            String sessionId = data.path("sessionID").asText();
            Long seq = event.path("durable").has("seq") ? event.path("durable").path("seq").asLong() : null;
            parser.parse(event).ifPresent(parsed -> {
                registry.publish(sessionId, seq, parsed);
                if (parsed instanceof StepEndedEvent) {
                    publishToolResults(sessionId);
                }
            });
        } catch (RuntimeException e) {
            log.warn("Cannot parse OpenCode event: {}", e.getMessage());
        }
    }

    private void publishToolResults(String sessionId) {
        try {
            for (ToolResultEvent result : ToolResults.parse(sessionId, client.sessionMessages(sessionId))) {
                registry.publish(sessionId, null, result);
            }
        } catch (RuntimeException e) {
            log.warn("Cannot read tool results of session {}: {}", sessionId, e.getMessage());
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
