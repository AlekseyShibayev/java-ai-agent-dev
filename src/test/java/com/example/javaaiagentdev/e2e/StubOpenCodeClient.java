package com.example.javaaiagentdev.e2e;

import com.example.javaaiagentdev.opencode.OpenCodeClient;
import com.example.javaaiagentdev.opencode.OpenCodeProperties;
import com.example.javaaiagentdev.opencode.OpenCodeUnavailableException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import tools.jackson.databind.ObjectMapper;

/**
 * OpenCode stand-in for the browser scenario: no HTTP, no provider, no credentials.
 * It feeds raw SSE frames into the real event pipeline, and the test decides when the next
 * chunk arrives, so a slow machine cannot make "the answer came in one piece" look true.
 */
final class StubOpenCodeClient extends OpenCodeClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
    private final AtomicLong seq = new AtomicLong();
    private final AtomicLong sessions = new AtomicLong();
    private final AtomicInteger aborts = new AtomicInteger();

    private volatile String sessionId = "ses_e2e";

    StubOpenCodeClient() {
        super(new OpenCodeProperties("http://127.0.0.1:1", "opencode", "", "", "ollama", "stub"), new ObjectMapper());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String createSession(String title) {
        sessionId = "ses_e2e_" + sessions.incrementAndGet();
        return sessionId;
    }

    @Override
    public void prompt(String sessionId, String text, String agent) {
        this.sessionId = sessionId;
    }

    @Override
    public void abort(String sessionId) {
        aborts.incrementAndGet();
        emit("session.execution.interrupted", Map.of("reason", "user"));
    }

    @Override
    public Stream<String> eventLines() {
        Iterator<String> lines = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                try {
                    return frames.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenCodeUnavailableException("Stub event stream interrupted", e);
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(lines, Spliterator.ORDERED), false);
    }

    int abortCount() {
        return aborts.get();
    }

    void emitTextDelta(String delta) {
        emit("session.text.delta", Map.of("assistantMessageID", "msg_e2e", "ordinal", 0, "delta", delta));
    }

    void emitTextEnded(String text) {
        emit("session.text.ended", Map.of("assistantMessageID", "msg_e2e", "ordinal", 0, "text", text));
    }

    void emitSucceeded() {
        emit("session.execution.succeeded", Map.of());
    }

    private void emit(String type, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>(data);
        payload.put("sessionID", sessionId);
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        event.put("data", payload);
        event.put("durable", Map.of("seq", seq.incrementAndGet()));
        frames.add("data: " + objectMapper.writeValueAsString(event));
        frames.add("");
    }
}
