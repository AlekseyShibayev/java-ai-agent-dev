package com.example.javaaiagentdev.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.javaaiagentdev.chat.RunRegistry;
import com.example.javaaiagentdev.opencode.OpenCodeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

class ChatPageControllerTest {

    private final OpenCodeClient client = mock(OpenCodeClient.class);
    private final RunRegistry registry = new RunRegistry();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatPageController(client, registry))
                .setViewResolvers((viewName, locale) -> (View) (model, request, response) -> {
                })
                .build();
    }

    @Test
    void shouldRenderEmptyChatPage() throws Exception {
        when(client.isAvailable()).thenReturn(true);

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("unavailable", false))
                .andExpect(model().attributeDoesNotExist("activeRunId"));
    }

    @Test
    void shouldRestoreActiveRunAfterReload() throws Exception {
        when(client.isAvailable()).thenReturn(true);
        String runId = registry.start("ses_1");
        MockHttpSession httpSession = new MockHttpSession();
        new ChatPageSession(httpSession).store("ses_1", runId);

        mockMvc.perform(get("/chat").session(httpSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeRunId", runId))
                .andExpect(model().attribute("sessionId", "ses_1"));
    }

    @Test
    void shouldShowUnavailableInsteadOfServerError() throws Exception {
        when(client.isAvailable()).thenReturn(false);

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("unavailable", true));
    }
}
