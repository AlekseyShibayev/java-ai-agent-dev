package com.example.javaaiagentdev.chat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** In-memory registry of chat runs. Events are deduplicated and fanned out to subscribers. */
public class RunRegistry {

    public static final int BUFFER_LIMIT = 1000;

    private final Map<String, Run> runs = new ConcurrentHashMap<>();

    public String start(String sessionId) {
        String runId = UUID.randomUUID().toString();
        runs.put(runId, new Run(runId, sessionId));
        return runId;
    }

    public Optional<Run> find(String runId) {
        return Optional.ofNullable(runs.get(runId));
    }

    public AutoCloseable subscribe(String runId, Consumer<RunEvent> listener) {
        Run run = runs.get(runId);
        if (run == null) {
            throw new IllegalArgumentException("Unknown run: " + runId);
        }
        synchronized (run) {
            for (RunEvent event : run.buffer) {
                listener.accept(event);
            }
            if (run.status != RunStatus.ACTIVE) {
                return () -> {
                };
            }
            run.listeners.add(listener);
        }
        return () -> {
            synchronized (run) {
                run.listeners.remove(listener);
            }
        };
    }

    public void publish(String sessionId, Long seq, RunEvent event) {
        for (Run run : runs.values()) {
            if (run.sessionId.equals(sessionId)) {
                run.accept(seq, event);
            }
        }
    }

    public enum RunStatus {
        ACTIVE, SUCCEEDED, INTERRUPTED, FAILED
    }

    public static final class Run {

        private final String runId;
        private final String sessionId;
        private final List<RunEvent> buffer = new ArrayList<>();
        private final Set<Long> seqs = new LinkedHashSet<>();
        private final List<Consumer<RunEvent>> listeners = new ArrayList<>();
        private RunStatus status = RunStatus.ACTIVE;
        private String error;

        private Run(String runId, String sessionId) {
            this.runId = runId;
            this.sessionId = sessionId;
        }

        public String runId() {
            return runId;
        }

        public String sessionId() {
            return sessionId;
        }

        public RunStatus status() {
            return status;
        }

        public String error() {
            return error;
        }

        private void accept(Long seq, RunEvent event) {
            synchronized (this) {
                if (status != RunStatus.ACTIVE || (seq != null && !seqs.add(seq))) {
                    return;
                }
                buffer.add(event);
                if (buffer.size() > BUFFER_LIMIT) {
                    fail("Run buffer overflow");
                    return;
                }
                listeners.forEach(listener -> listener.accept(event));
                if (event instanceof ExecutionSucceededEvent) {
                    status = RunStatus.SUCCEEDED;
                } else if (event instanceof ExecutionInterruptedEvent) {
                    status = RunStatus.INTERRUPTED;
                }
                if (status != RunStatus.ACTIVE) {
                    listeners.clear();
                }
            }
        }

        private void fail(String message) {
            error = message;
            status = RunStatus.FAILED;
            listeners.clear();
        }
    }
}
