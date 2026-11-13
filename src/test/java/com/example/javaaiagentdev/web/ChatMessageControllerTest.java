package com.example.javaaiagentdev.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import org.springframework.web.servlet.View;

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
                .setViewResolvers((viewName, locale) -> (View) (model, request, response) -> {
                })
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
    void shouldRenderRunFragmentForHtmxRequest() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));

        mockMvc.perform(post("/chat/messages")
                        .session(session)
                        .header("HX-Request", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "hi"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "hi"))
                .andExpect(model().attributeExists("runId"))
                .andExpect(view().name("fragments/run"));
    }

    @Test
    void shouldStopRunAndReturnIdleComposer() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));
        postMessage("hi").andExpect(status().isAccepted());

        mockMvc.perform(post("/chat/stop")
                        .session(session)
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/composer :: composer(false, false)"));

        verify(service).abort("ses_1");
    }

    @Test
    void shouldNotAbortWhenRunIsAlreadyOver() throws Exception {
        when(client.createSession("chat")).thenReturn("ses_1");
        when(service.start("ses_1", "hi", null)).thenAnswer(invocation -> registry.start("ses_1"));
        postMessage("hi").andExpect(status().isAccepted());
        registry.publish("ses_1", 1L, new ExecutionSucceededEvent("ses_1"));

        mockMvc.perform(post("/chat/stop").session(session).header("HX-Request", "true"))
                .andExpect(status().isOk());

        verify(service, never()).abort("ses_1");
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
