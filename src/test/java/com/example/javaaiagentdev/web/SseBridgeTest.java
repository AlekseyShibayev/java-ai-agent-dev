package com.example.javaaiagentdev.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javaaiagentdev.chat.ExecutionSucceededEvent;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.chat.StepEndedEvent;
import com.example.javaaiagentdev.chat.TextDeltaEvent;
import com.example.javaaiagentdev.chat.ToolCalledEvent;
import com.example.javaaiagentdev.chat.ToolFailedEvent;
import com.example.javaaiagentdev.chat.ToolResultEvent;
import com.example.javaaiagentdev.chat.ToolStartedEvent;
import com.example.javaaiagentdev.chat.Usage;
import com.example.javaaiagentdev.chat.UsageUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class SseBridgeTest {

    private final RunRegistry registry = new RunRegistry();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SseBridge(registry, new ObjectMapper()))
                .setControllerAdvice(new com.example.javaaiagentdev.chat.ChatErrorHandler())
                .build();
    }

    @Test
    void shouldSendNamedTextEventWithEscapedDeltaAndStateAtTheEnd() throws Exception {
        String runId = registry.start("ses_1");
        registry.publish("ses_1", 1L, new TextDeltaEvent("ses_1", "msg_1", 0, "<b>hi</b>\n"));
        registry.publish("ses_1", 2L, new ExecutionSucceededEvent("ses_1"));

        MvcResult result = mockMvc.perform(get("/chat/runs/" + runId + "/events").accept("text/event-stream"))
                .andExpect(request().asyncStarted())
                .andReturn();
        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains("event:text"), body);
        assertTrue(body.contains("id:1"), body);
        assertTrue(body.contains("&lt;b&gt;hi&lt;/b&gt;"), body);
        assertFalse(body.contains("<b>hi</b>"), body);
        assertTrue(body.contains("event:state"), body);
        assertTrue(body.contains("\"state\":\"succeeded\""), body);
    }

    @Test
    void shouldNameToolAndUsageEventsByKind() throws Exception {
        String runId = registry.start("ses_1");
        ObjectMapper mapper = new ObjectMapper();
        registry.publish("ses_1", 1L, new ToolStartedEvent("ses_1", "msg_1", "call_1", "read"));
        registry.publish("ses_1", 2L, new ToolCalledEvent("ses_1", "msg_1", "call_1",
                mapper.readTree("{\"path\":\"/repo/README.md\"}")));
        registry.publish("ses_1", 3L, new ToolResultEvent("ses_1", "call_1", "completed", "Read file"));
        registry.publish("ses_1", 4L, new ToolFailedEvent("ses_1", "msg_1", "call_1", "tool_error", "boom"));
        registry.publish("ses_1", 5L, new StepEndedEvent("ses_1", "msg_1", "stop", 0.000058,
                new Usage(530, 10, 0, 0, 0)));
        registry.publish("ses_1", 6L, new UsageUpdatedEvent("ses_1", 0.000058,
                new Usage(530, 10, 0, 0, 0)));
        registry.publish("ses_1", 7L, new ExecutionSucceededEvent("ses_1"));

        MvcResult result = mockMvc.perform(get("/chat/runs/" + runId + "/events").accept("text/event-stream"))
                .andExpect(request().asyncStarted())
                .andReturn();
        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains("event:tool.started"), body);
        assertTrue(body.contains("event:tool.called"), body);
        assertTrue(body.contains("event:tool.result"), body);
        assertTrue(body.contains("event:tool.failed"), body);
        assertTrue(body.contains("event:usage.step"), body);
        assertTrue(body.contains("event:usage.session"), body);
    }

    @Test
    void shouldSkipEventsAlreadySeenByTheBrowser() throws Exception {
        String runId = registry.start("ses_1");
        registry.publish("ses_1", 1L, new TextDeltaEvent("ses_1", "msg_1", 0, "old "));
        registry.publish("ses_1", 2L, new TextDeltaEvent("ses_1", "msg_1", 0, "seen "));

        MvcResult result = mockMvc.perform(get("/chat/runs/" + runId + "/events")
                        .accept("text/event-stream")
                        .header("Last-Event-ID", "2"))
                .andExpect(request().asyncStarted())
                .andReturn();

        registry.publish("ses_1", 3L, new TextDeltaEvent("ses_1", "msg_1", 0, "new "));
        registry.publish("ses_1", 4L, new ExecutionSucceededEvent("ses_1"));

        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(body.contains("old "), body);
        assertFalse(body.contains("seen "), body);
        assertTrue(body.contains("new "), body);
        assertTrue(body.contains("id:3"), body);
    }

    @Test
    void shouldStopReconnectingWhenRunIsOver() throws Exception {
        String runId = registry.start("ses_1");
        registry.publish("ses_1", 1L, new ExecutionSucceededEvent("ses_1"));

        mockMvc.perform(get("/chat/runs/" + runId + "/events")
                        .accept("text/event-stream")
                        .header("Last-Event-ID", "1"))
                .andExpect(status().isNoContent())
                .andExpect(request().asyncNotStarted());
    }

    @Test
    void shouldReturnNotFoundForUnknownRun() throws Exception {
        assertEquals(404, mockMvc.perform(get("/chat/runs/unknown/events")).andReturn().getResponse().getStatus());
    }
}
