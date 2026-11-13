package com.example.javaaiagentdev.web;

import com.example.javaaiagentdev.chat.ChatService;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Skeleton of the message endpoint; filled in by the green step. */
@RestController
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

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> send(@RequestBody SendMessageRequest request, HttpSession httpSession) {
        return Map.of();
    }

    public record SendMessageRequest(String text, String agent) {
    }
}
