package com.example.javaaiagentdev.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ToolResultsTest {

    @Test
    void shouldExtractToolResultFromAssistantMessage() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/opencode/tool.messages.json")) {
            List<ToolResultEvent> results = ToolResults.parse("ses_test", new ObjectMapper().readTree(input));

            assertEquals(1, results.size());
            assertEquals("call_test", results.get(0).callId());
            assertEquals("completed", results.get(0).status());
            assertTrue(results.get(0).content().contains("Read file /repo/README.md"), results.get(0).content());
        }
    }
}
