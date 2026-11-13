package com.example.javaaiagentdev.chat;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;
    private final RunRegistry registry;
    private final ObjectMapper objectMapper;

    public ChatController(ChatService service, RunRegistry registry, ObjectMapper objectMapper) {
        this.service = service;
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{sessionId}/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public StartRunResponse start(@PathVariable String sessionId, @RequestBody StartMessageRequest request) {
        return new StartRunResponse(service.start(sessionId, request.text(), request.agent()));
    }

    @PostMapping("/{sessionId}/abort")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void abort(@PathVariable String sessionId) {
        service.abort(sessionId);
    }

    @PostMapping("/{sessionId}/permission/{requestId}")
    public ResponseEntity<Void> permission(@PathVariable String sessionId, @PathVariable String requestId,
            @RequestBody PermissionRequest request) {
        if (!"allow".equals(request.decision()) && !"reject".equals(request.decision())) {
            throw new IllegalArgumentException("Decision must be allow or reject");
        }
        service.replyPermission(sessionId, requestId, request.decision());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{sessionId}/runs/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable String sessionId, @PathVariable String runId) {
        registry.find(runId)
                .filter(run -> run.sessionId().equals(sessionId))
                .orElseThrow(() -> new NoSuchElementException("Unknown run: " + runId));

        SseEmitter emitter = new SseEmitter(0L);
        AutoCloseable subscription = registry.subscribe(runId, event -> send(emitter, event));
        emitter.onCompletion(() -> close(subscription));
        emitter.onTimeout(() -> close(subscription));
        emitter.onError(error -> close(subscription));
        return emitter;
    }

    private void send(SseEmitter emitter, RunEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(RunEventNames.of(event))
                    .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
            if (event instanceof ExecutionSucceededEvent || event instanceof ExecutionInterruptedEvent) {
                emitter.complete();
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private static void close(AutoCloseable subscription) {
        try {
            subscription.close();
        } catch (Exception ignored) {
            // Nothing to do: the subscription is already gone.
        }
    }

    public record StartMessageRequest(String text, String agent) {
    }

    public record StartRunResponse(String runId) {
    }

    public record PermissionRequest(String decision) {
    }
}
