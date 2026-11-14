package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChatMultipleAnswersE2ETest extends PlaywrightE2ETest {

    @Test
    void shouldWriteSecondAnswerToItsOwnBlock() {
        open();
        addSecondAnswer();

        emit(1, "text", "<span>второй</span>");

        assertEquals("первый", answerText(0));
        assertEquals("второй", answerText(1));
    }

    @Test
    void shouldConnectSecondAnswerToItsOwnRun() {
        open();
        addSecondAnswer();

        assertEquals(2, sourceCount());
        assertEquals("fixture://run-2", sourceUrl(1));
    }

    @Test
    void shouldScopeReasoningToolsAndUsageToActiveAnswer() {
        open();
        addSecondAnswer();

        emit(1, "reasoning", "{\"sessionId\":\"ses_1\",\"ordinal\":0,\"delta\":\"думаю\"}");
        emit(1, "tool.called",
                "{\"sessionId\":\"ses_1\",\"callId\":\"call_1\",\"name\":\"shell\",\"input\":{\"cmd\":\"ls\"}}");
        emit(1, "usage.step", "{\"sessionId\":\"ses_1\",\"cost\":0.5,\"tokens\":{\"input\":10,\"output\":2}}");
        emit(1, "state", "{\"state\":\"succeeded\"}");

        assertEquals(0, answer(0).locator(".reasoning").count(), "previous answer must stay untouched");
        assertEquals(0, answer(0).locator(".tool-card").count(), "previous answer must stay untouched");
        assertTrue(answer(0).locator(".run-usage").isHidden(), "previous usage must stay hidden");
        assertEquals(1, answer(1).locator(".reasoning").count());
        assertEquals(1, answer(1).locator(".tool-card").count());
        assertTrue(answer(1).locator(".run-usage").isVisible());
    }

    private void open() {
        session.openResource("/e2e/chat-multiple-answers.html");
    }

    private void addSecondAnswer() {
        session.page().evaluate("() => window.__addAnswer('fixture://run-2', 'ses_1', '')");
    }

    private Locator answer(int index) {
        return session.page().locator(".answer").nth(index);
    }

    private String answerText(int index) {
        return answer(index).locator(".answer-text").textContent();
    }

    private int sourceCount() {
        return ((Number) session.page().evaluate("window.__sources.length")).intValue();
    }

    private String sourceUrl(int index) {
        return (String) session.page().evaluate("index => window.__sources[index].url", index);
    }

    private void emit(int index, String name, String data) {
        session.page().evaluate("payload => window.__emit(payload.index, payload.name, payload.data)",
                Map.of("index", index, "name", name, "data", data));
    }
}
