package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Skeleton of the chat page; filled in by the green step. */
@Controller
public class ChatPageController {

    private final OpenCodeClient client;
    private final RunRegistry registry;

    public ChatPageController(OpenCodeClient client, RunRegistry registry) {
        this.client = client;
        this.registry = registry;
    }

    @GetMapping("/chat")
    public String page() {
        return "chat";
    }
}
