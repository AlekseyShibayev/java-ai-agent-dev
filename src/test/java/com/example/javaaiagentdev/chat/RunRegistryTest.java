package com.example.javaaiagentdev.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RunRegistryTest {

    private final RunRegistry registry = new RunRegistry();

    @Test
    void shouldIgnoreDuplicateSeq() {
        String runId = registry.start("ses_test");
        List<RunEvent> received = new ArrayList<>();
        registry.subscribe(runId, received::add);

        TextDeltaEvent event = new TextDeltaEvent("ses_test", "msg_test", 0, "ok");
        registry.publish("ses_test", 10L, event);
        registry.publish("ses_test", 10L, event);

        assertEquals(1, received.size());
    }

    @Test
    void shouldIgnoreForeignSession() {
        String runId = registry.start("ses_mine");
        List<RunEvent> received = new ArrayList<>();
        registry.subscribe(runId, received::add);

        registry.publish("ses_other", 10L, new TextDeltaEvent("ses_other", "msg_test", 0, "no"));

        assertTrue(received.isEmpty());
    }

    @Test
    void shouldKeepRunActiveOnToolFailure() {
        String runId = registry.start("ses_test");
        registry.publish("ses_test", 10L, new ToolFailedEvent("ses_test", "msg_test", "call_test", "aborted", "declined"));

        assertEquals(RunRegistry.RunStatus.ACTIVE, registry.find(runId).orElseThrow().status());
    }

    @Test
    void shouldCloseRunOnSucceeded() {
        String runId = registry.start("ses_test");
        List<RunEvent> received = new ArrayList<>();
        registry.subscribe(runId, received::add);

        registry.publish("ses_test", 10L, new ExecutionSucceededEvent("ses_test"));

        assertEquals(RunRegistry.RunStatus.SUCCEEDED, registry.find(runId).orElseThrow().status());
        assertEquals(1, received.size());
    }

    @Test
    void shouldCloseRunOnInterrupted() {
        String runId = registry.start("ses_test");
        registry.publish("ses_test", 10L, new ExecutionInterruptedEvent("ses_test", "user"));

        assertEquals(RunRegistry.RunStatus.INTERRUPTED, registry.find(runId).orElseThrow().status());
    }

    @Test
    void shouldReplayBufferedEventsOnSubscribe() {
        String runId = registry.start("ses_test");
        registry.publish("ses_test", 10L, new TextDeltaEvent("ses_test", "msg_test", 0, "buffered"));

        List<RunEvent> received = new ArrayList<>();
        registry.subscribe(runId, received::add);

        assertEquals(List.of(new TextDeltaEvent("ses_test", "msg_test", 0, "buffered")), received);
    }
}
