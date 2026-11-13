package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.RunRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

/** Skeleton of the browser event stream; filled in by the green step. */
@Controller
public class SseBridge {

    private final RunRegistry registry;
    private final ObjectMapper objectMapper;

    public SseBridge(RunRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/chat/runs/{runId}/events")
    public ResponseEntity<SseEmitter> events(@PathVariable String runId) {
        return ResponseEntity.noContent().build();
    }
}
