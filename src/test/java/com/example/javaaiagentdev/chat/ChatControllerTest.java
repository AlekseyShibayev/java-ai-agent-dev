package com.example.javaaiagentdev.chat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.javaaiagentdev.opencode.OpenCodeUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class ChatControllerTest {

    private final ChatService service = mock(ChatService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(service, new RunRegistry(), new ObjectMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ChatErrorHandler())
                .build();
    }

    @Test
    void shouldStartRun() throws Exception {
        when(service.start("ses_1", "hi", "build")).thenReturn("run_1");

        mockMvc.perform(post("/api/chat/ses_1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hi\",\"agent\":\"build\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value("run_1"));
    }

    @Test
    void shouldReturnBadGatewayWhenOpenCodeUnavailable() throws Exception {
        when(service.start("ses_1", "hi", null)).thenThrow(new OpenCodeUnavailableException("down"));

        mockMvc.perform(post("/api/chat/ses_1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hi\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldAbortRun() throws Exception {
        mockMvc.perform(post("/api/chat/ses_1/abort"))
                .andExpect(status().isAccepted());

        verify(service).abort("ses_1");
    }

    @Test
    void shouldReplyPermission() throws Exception {
        mockMvc.perform(post("/api/chat/ses_1/permission/per_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"reject\"}"))
                .andExpect(status().isNoContent());

        verify(service).replyPermission("ses_1", "per_1", "reject");
    }

    @Test
    void shouldRejectUnknownDecision() throws Exception {
        mockMvc.perform(post("/api/chat/ses_1/permission/per_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"maybe\"}"))
                .andExpect(status().isBadRequest());
    }
}
