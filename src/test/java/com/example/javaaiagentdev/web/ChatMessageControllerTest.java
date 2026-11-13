package com.example.javaaiagentdev.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javaaiagentdev.chat.ChatErrorHandler;
import com.example.javaaiagentdev.chat.ChatService;
import com.example.javaaiagentdev.chat.ExecutionSucceededEvent;
import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ChatMessageControllerTest {

    private final ChatService service = mock(ChatService.class);
    private final OpenCodeClient client = mock(OpenCodeClient.class);
    private final RunRegistry registry = new RunRegistry();
    private final MockHttpSession session = new MockHttpSession();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatMessageController(service, client, registry))
                .setControllerAdvice(new ChatErrorHandler())
                .build();
    }

    @Test
    void shouldCreateSessionAndStartRun() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));

        postMessage("hi")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").exists())
                .andExpect(jsonPath("$.sessionId").value("ses_1"));
    }

    @Test
    void shouldReuseSessionForNextMessage() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));
        when(service.start("ses_1", "again", null)).thenAnswer(invocation -> registry.start("ses_1"));

        postMessage("hi").andExpect(status().isAccepted());
        registry.publish("ses_1", 1L, new ExecutionSucceededEvent("ses_1"));
        postMessage("again").andExpect(status().isAccepted());

        verify(client, times(1)).createSession("chat");
        verify(service).start("ses_1", "again", null);
    }

    @Test
    void shouldRejectSecondPromptWhileRunIsActive() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));

        postMessage("hi").andExpect(status().isAccepted());

        postMessage("one more").andExpect(status().isConflict());

        verify(service, times(1)).start("ses_1", "hi", null);
        verify(service, never()).start("ses_1", "one more", null);
    }

    private org.springframework.test.web.servlet.ResultActions postMessage(String text) throws Exception {
        return mockMvc.perform(post("/chat/messages")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"" + text + "\"}"));
    }
}
