package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChatToolsE2ETest extends PlaywrightE2ETest {

    @Test
    void shouldShowCollapsedToolCardWithInputAndResult() {
        open();

        emit("tool.started", "{\"callId\":\"call_1\",\"name\":\"read\"}");
        emit("tool.called", "{\"callId\":\"call_1\",\"input\":{\"path\":\"/repo/README.md\",\"limit\":20}}");

        Locator card = session.page().locator(".tool-card");
        assertEquals(1, card.count());
        assertNull(card.getAttribute("open"), "tool card must be collapsed");
        assertTrue(card.locator("summary").textContent().contains("read"));
        assertTrue(card.locator(".tool-input").textContent().contains("/repo/README.md"));

        emit("tool.result", "{\"callId\":\"call_1\",\"status\":\"completed\",\"content\":\"Read file /repo/README.md\"}");

        assertEquals(1, session.page().locator(".tool-card").count());
        assertTrue(card.locator(".tool-result").textContent().contains("Read file /repo/README.md"));
        assertNull(card.getAttribute("open"), "tool card must stay collapsed");
    }

    @Test
    void shouldIgnoreResultOfUnknownCall() {
        open();
        emit("tool.started", "{\"callId\":\"call_1\",\"name\":\"read\"}");
        emit("tool.called", "{\"callId\":\"call_1\",\"input\":{\"path\":\"/repo/README.md\"}}");

        emit("tool.result", "{\"callId\":\"call_old\",\"status\":\"completed\",\"content\":\"old result\"}");

        assertEquals(1, session.page().locator(".tool-card").count());
        assertFalse(session.page().locator(".tool-card").textContent().contains("old result"));
    }

    @Test
    void shouldShowToolInputAsText() {
        open();

        emit("tool.started", "{\"callId\":\"call_1\",\"name\":\"search\"}");
        emit("tool.called", "{\"callId\":\"call_1\",\"input\":{\"q\":\"<img src=x onerror=window.__xss=1>\"}}");

        assertEquals(0, session.page().locator(".tool-card img").count());
        assertTrue(session.page().locator(".tool-input").textContent().contains("<img src=x"));
        assertNull(session.page().evaluate("window.__xss"));
    }

    @Test
    void shouldKeepReasoningCollapsedAndAppendDeltasToTheSameNode() {
        open();

        emit("reasoning", "{\"delta\":\"plan \"}");
        session.page().evaluate("window.__reasoning = document.querySelector('.reasoning-text')");
        emit("reasoning", "{\"delta\":\"next step\"}");

        Locator reasoning = session.page().locator("details.reasoning");
        assertEquals(1, reasoning.count());
        assertNull(reasoning.getAttribute("open"), "reasoning must be collapsed");
        assertEquals("plan next step", reasoning.locator(".reasoning-text").textContent());
        assertTrue((Boolean) session.page().evaluate(
                "window.__reasoning === document.querySelector('.reasoning-text')"),
                "reasoning node must not be replaced");
    }

    @Test
    void shouldShowRunCostAndTokensAfterCompletion() {
        open();

        emit("usage.step", "{\"cost\":0.000029,\"tokens\":{\"input\":265,\"output\":5}}");
        emit("usage.step", "{\"cost\":0.000029,\"tokens\":{\"input\":265,\"output\":5}}");
        emit("state", "{\"state\":\"succeeded\"}");

        Locator usage = session.page().locator(".run-usage");
        assertTrue(usage.isVisible());
        assertEquals("$0.000058", session.page().locator(".run-cost").textContent().trim());
        String tokens = session.page().locator(".run-tokens").textContent();
        assertTrue(tokens.contains("530"), tokens);
        assertTrue(tokens.contains("10"), tokens);
    }

    @Test
    void shouldShowToolFailureWithoutFailingTheRun() {
        open();
        emit("state", "{\"state\":\"started\"}");

        emit("tool.started", "{\"callId\":\"call_1\",\"name\":\"read\"}");
        emit("tool.failed", "{\"callId\":\"call_1\",\"errorType\":\"tool_error\",\"message\":\"boom\"}");

        Locator failure = session.page().locator(".tool-card.tool-error");
        assertEquals(1, failure.count());
        assertTrue(failure.textContent().contains("boom"));
        assertFalse(session.page().locator("#send").isEnabled(), "run must stay active");
        assertFalse(session.page().locator(".run-usage").isVisible());
    }

    @Test
    void shouldNotShowFalseToolErrorAfterAbort() {
        open();
        emit("state", "{\"state\":\"started\"}");

        emit("tool.started", "{\"callId\":\"call_1\",\"name\":\"bash\"}");
        emit("tool.failed", "{\"callId\":\"call_1\",\"errorType\":\"aborted\",\"message\":\"Step interrupted\"}");
        emit("state", "{\"state\":\"interrupted\"}");

        assertEquals(1, session.page().locator(".tool-card").count());
        assertEquals(0, session.page().locator(".tool-error").count());
        assertTrue(session.page().locator("#send").isEnabled());
    }

    private void open() {
        session.openResource("/e2e/chat-tools.html");
    }

    private void emit(String name, String json) {
        session.page().evaluate("payload => window.__emit(payload.name, payload.data)",
                Map.of("name", name, "data", json));
    }
}
