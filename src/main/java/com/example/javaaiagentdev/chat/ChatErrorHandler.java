package com.example.javaaiagentdev.chat;

import java.util.Map;
import java.util.NoSuchElementException;
import com.example.javaaiagentdev.opencode.OpenCodeUnavailableException;
import com.example.javaaiagentdev.web.ChatMessageController;
import com.example.javaaiagentdev.web.SseBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {ChatController.class, ChatMessageController.class, SseBridge.class})
public class ChatErrorHandler {

    @ExceptionHandler(OpenCodeUnavailableException.class)
    public ResponseEntity<Map<String, String>> unavailable(OpenCodeUnavailableException error) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "OpenCode is unavailable"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }
}
