package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.chat.RunRegistry.Run;
import com.example.javaaiagentdev.chat.RunRegistry.RunStatus;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Renders the chat page and restores an active run after a reload. */
@Controller
public class ChatPageController {

    private final OpenCodeClient client;
    private final RunRegistry registry;

    public ChatPageController(OpenCodeClient client, RunRegistry registry) {
        this.client = client;
        this.registry = registry;
    }

    @GetMapping("/chat")
    public String page(HttpSession httpSession, Model model) {
        ChatPageSession session = new ChatPageSession(httpSession);
        model.addAttribute("unavailable", !client.isAvailable());
        model.addAttribute("sessionId", session.openCodeSessionId());
        session.runId()
                .flatMap(registry::find)
                .filter(run -> run.status() == RunStatus.ACTIVE)
                .map(Run::runId)
                .ifPresent(runId -> model.addAttribute("activeRunId", runId));
        return "chat";
    }
}
