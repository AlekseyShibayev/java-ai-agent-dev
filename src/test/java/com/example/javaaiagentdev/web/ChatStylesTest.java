package com.example.javaaiagentdev.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/** Стили страницы чата: локальное подключение, серверная отдача, тема оформления. */
@SpringBootTest
@ActiveProfiles("test")
class ChatStylesTest {

    private static final String STYLESHEET = "static/css/chat.css";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private SpringTemplateEngine engine;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Test
    void shouldLinkLocalStylesheet() {
        Context model = new Context();
        model.setVariable("unavailable", false);

        String html = engine.process("chat", model);

        assertTrue(html.contains("<link rel=\"stylesheet\" href=\"/css/chat.css\">"), html);
        assertFalse(html.contains("href=\"http"), html);
    }

    @Test
    void shouldServeStylesheetAsCss() throws Exception {
        mockMvc.perform(get("/css/chat.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/css")));
    }

    @Test
    void shouldShipThemeVariables() throws Exception {
        ClassPathResource stylesheet = new ClassPathResource(STYLESHEET);
        assertTrue(stylesheet.exists(), STYLESHEET + " not found");

        String css = stylesheet.getContentAsString(StandardCharsets.UTF_8);

        assertTrue(css.contains("--"), "no CSS variables");
        assertTrue(css.contains("prefers-color-scheme: dark"), "no dark theme");
    }
}
