package com.example.javaaiagentdev.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class ChatMarkupTest {

    private SpringTemplateEngine engine;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Test
    void shouldKeepLineBreaksOfTheAnswer() {
        Context context = new Context();
        context.setVariable("runId", "run_1");

        String html = engine.process("fragments/run", context);

        assertTrue(html.contains("white-space: pre-wrap"), html);
    }

    @Test
    void shouldRenderChatPage() {
        Context context = new Context();
        context.setVariable("unavailable", false);

        String html = engine.process("chat", context);

        assertTrue(html.contains("id=\"composer\""), html);
        assertTrue(html.contains("id=\"thread\""), html);
        assertTrue(html.contains("/js/htmx.min.js"), html);
        assertTrue(html.contains("/js/chat.js"), html);
    }

    @Test
    void shouldEscapeModelHtmlAndConnectAnswerToRun() {
        Context context = new Context();
        context.setVariable("message", "<script>alert('x')</script>");
        context.setVariable("runId", "run_1");

        String html = engine.process("fragments/run", context);

        assertFalse(html.contains("<script>alert"), html);
        assertTrue(html.contains("&lt;script&gt;alert"), html);
        assertTrue(html.contains("data-sse-url=\"/chat/runs/run_1/events\""), html);
    }
}
