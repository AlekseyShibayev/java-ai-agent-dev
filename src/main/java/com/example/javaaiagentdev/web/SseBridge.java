package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.ExecutionInterruptedEvent;
import com.example.javaaiagentdev.chat.ExecutionStartedEvent;
import com.example.javaaiagentdev.chat.ExecutionSucceededEvent;
import com.example.javaaiagentdev.chat.RunEvent;
import com.example.javaaiagentdev.chat.RunEventNames;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.chat.RunRegistry.Run;
import com.example.javaaiagentdev.chat.RunRegistry.RunStatus;
import com.example.javaaiagentdev.chat.TextDeltaEvent;
import com.example.javaaiagentdev.chat.TextEndedEvent;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

/**
 * Browser event stream of a run. Every event gets an {@code id}, so the HTMX SSE extension can
 * resume with {@code Last-Event-ID} without repeating text the page already shows.
 */
@Controller
public class SseBridge {

    private final RunRegistry registry;
    private final ObjectMapper objectMapper;

    public SseBridge(RunRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @GetMapping(path = "/chat/runs/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> events(@PathVariable String runId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        Run run = registry.find(runId).orElseThrow(() -> new NoSuchElementException("Unknown run: " + runId));
        int after = parseId(lastEventId);
        if (run.status() != RunStatus.ACTIVE && run.eventCount() <= after) {
            return ResponseEntity.noContent().build();
        }

        SseEmitter emitter = new SseEmitter(0L);
        AtomicInteger nextId = new AtomicInteger(1);
        Set<Integer> textOrdinals = ConcurrentHashMap.newKeySet();
        AutoCloseable subscription = registry.subscribe(runId, event -> {
            int id = nextId.getAndIncrement();
            if (id <= after) {
                return;
            }
            send(emitter, id, event, textOrdinals);
        });
        emitter.onCompletion(() -> close(subscription));
        emitter.onTimeout(() -> close(subscription));
        emitter.onError(error -> close(subscription));
        return ResponseEntity.ok(emitter);
    }

    private void send(SseEmitter emitter, int id, RunEvent event, Set<Integer> textOrdinals) {
        SseEmitter.SseEventBuilder message = SseEmitter.event()
                .id(String.valueOf(id))
                .name(RunEventNames.of(event));
        try {
            if (event instanceof TextDeltaEvent delta) {
                textOrdinals.add(delta.ordinal());
                message.data(text(delta.delta()), MediaType.TEXT_HTML);
            } else if (event instanceof TextEndedEvent ended) {
                if (textOrdinals.contains(ended.ordinal())) {
                    return;
                }
                message.data(text(ended.text()), MediaType.TEXT_HTML);
            } else if (event instanceof ExecutionStartedEvent) {
                message.data(state("started"), MediaType.APPLICATION_JSON);
            } else if (event instanceof ExecutionSucceededEvent || event instanceof ExecutionInterruptedEvent) {
                message.data(state(event instanceof ExecutionSucceededEvent ? "succeeded" : "interrupted"),
                        MediaType.APPLICATION_JSON);
                emitter.send(message);
                emitter.complete();
                return;
            } else {
                message.data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON);
            }
            emitter.send(message);
        } catch (IOException | RuntimeException e) {
            emitter.completeWithError(e);
        }
    }

    private String state(String state) {
        return objectMapper.writeValueAsString(Map.of("state", state));
    }

    private static String text(String value) {
        return "<span>" + HtmlUtils.htmlEscape(value == null ? "" : value) + "</span>";
    }

    private static int parseId(String value) {
        try {
            return value == null ? 0 : Math.max(0, Integer.parseInt(value.strip()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void close(AutoCloseable subscription) {
        try {
            subscription.close();
        } catch (Exception ignored) {
            // The subscription is already gone.
        }
    }
}
