package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.ChatService;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.chat.RunRegistry.RunStatus;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Starts a chat run from the page (HTMX) or from the JSON API of the runtime. */
@Controller
@RequestMapping("/chat")
public class ChatMessageController {

    private final ChatService service;
    private final OpenCodeClient client;
    private final RunRegistry registry;

    public ChatMessageController(ChatService service, OpenCodeClient client, RunRegistry registry) {
        this.service = service;
        this.client = client;
        this.registry = registry;
    }

    @PostMapping(value = "/messages", headers = "HX-Request")
    public Object sendFromPage(@ModelAttribute SendMessageRequest request, HttpSession httpSession, Model model) {
        if (isBusy(httpSession)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Run already active"));
        }
        String text = text(request.text());
        StartRunResponse started = start(text, request.agent(), httpSession);
        model.addAttribute("message", text);
        model.addAttribute("runId", started.runId());
        return "fragments/run";
    }

    @PostMapping(value = "/messages", headers = "!HX-Request")
    @ResponseBody
    public ResponseEntity<Object> send(@RequestBody SendMessageRequest request, HttpSession httpSession) {
        if (isBusy(httpSession)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Run already active"));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(start(text(request.text()), request.agent(), httpSession));
    }

    @PostMapping(value = "/stop", headers = "HX-Request")
    public String stop(HttpSession httpSession) {
        ChatPageSession page = new ChatPageSession(httpSession);
        if (isBusy(httpSession) && page.openCodeSessionId() != null) {
            service.abort(page.openCodeSessionId());
        }
        return "fragments/composer :: composer(busy=false, oob=false)";
    }

    private StartRunResponse start(String text, String agent, HttpSession httpSession) {
        ChatPageSession page = new ChatPageSession(httpSession);
        String sessionId = page.openCodeSessionId();
        if (sessionId == null) {
            sessionId = client.createSession("chat");
        }
        String runId = service.start(sessionId, text, agent);
        page.store(sessionId, runId);
        return new StartRunResponse(runId, sessionId);
    }

    private boolean isBusy(HttpSession httpSession) {
        return new ChatPageSession(httpSession).runId()
                .flatMap(registry::find)
                .filter(run -> run.status() == RunStatus.ACTIVE)
                .isPresent();
    }

    private static String text(String value) {
        String text = value == null ? "" : value.strip();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Message text must not be blank");
        }
        return text;
    }

    public record SendMessageRequest(String text, String agent) {
    }

    public record StartRunResponse(String runId, String sessionId) {
    }
}
