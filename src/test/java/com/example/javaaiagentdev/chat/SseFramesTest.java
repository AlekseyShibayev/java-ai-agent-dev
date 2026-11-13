package com.example.javaaiagentdev.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SseFramesTest {

    @Test
    void shouldSkipHeartbeat() {
        List<String> frames = new ArrayList<>();
        SseFrames sse = new SseFrames();

        sse.accept(": heartbeat", frames::add);
        sse.accept("", frames::add);

        assertEquals(List.of(), frames);
    }

    @Test
    void shouldEmitDataFrameOnBlankLine() {
        List<String> frames = new ArrayList<>();
        SseFrames sse = new SseFrames();

        sse.accept("data: {\"type\":\"session.text.delta\"}", frames::add);
        sse.accept("", frames::add);

        assertEquals(List.of("{\"type\":\"session.text.delta\"}"), frames);
    }

    @Test
    void shouldJoinMultilineData() {
        List<String> frames = new ArrayList<>();
        SseFrames sse = new SseFrames();

        sse.accept("data: first", frames::add);
        sse.accept("data: second", frames::add);
        sse.accept("", frames::add);

        assertEquals(List.of("first\nsecond"), frames);
    }
}
