package com.example.javaaiagentdev.chat;

import java.util.function.Consumer;

/** Splits an SSE stream into frames. Comments such as {@code : heartbeat} are ignored. */
public class SseFrames {

    private final StringBuilder data = new StringBuilder();

    public void accept(String line, Consumer<String> onFrame) {
        if (line.isEmpty()) {
            flush(onFrame);
        } else if (line.startsWith(":")) {
            return;
        } else if (line.startsWith("data:")) {
            if (data.length() > 0) {
                data.append('\n');
            }
            data.append(line.substring("data:".length()).stripLeading());
        }
    }

    private void flush(Consumer<String> onFrame) {
        if (data.length() > 0) {
            String frame = data.toString();
            data.setLength(0);
            onFrame.accept(frame);
        }
    }
}
