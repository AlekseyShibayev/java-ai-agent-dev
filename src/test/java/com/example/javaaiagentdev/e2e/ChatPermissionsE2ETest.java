package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class ChatPermissionsE2ETest extends PlaywrightE2ETest {

    @Test
    void shouldShowPermissionCardWithActionAndResources() {
        open();

        asked("per_1", "shell", "[\"echo opencode-probe\"]");

        Locator card = card();
        assertEquals(1, card.count());
        assertTrue(card.locator(".permission-action").textContent().contains("shell"));
        assertTrue(card.locator(".permission-resources").textContent().contains("echo opencode-probe"));
    }

    @Test
    void shouldShowPermissionDataAsText() {
        open();

        asked("per_1", "<img src=x onerror=window.__xss=1>", "[\"<b>resource</b>\"]");

        assertEquals(0, session.page().locator(".permission-card img").count());
        assertEquals(0, session.page().locator(".permission-card b").count());
        assertTrue(card().textContent().contains("<img src=x"));
        assertNull(session.page().evaluate("window.__xss"));
    }

    @Test
    void shouldSendAllowOnceAndCloseTheCard() {
        open();
        emit("state", "{\"state\":\"started\"}");
        asked("per_1", "shell", "[\"echo hi\"]");

        clickTwice(".permission-allow");
        await(() -> replyCount() == 1);

        assertEquals("/api/chat/ses_1/permission/per_1", lastReply("url"));
        assertTrue(lastReply("body").contains("\"decision\":\"allow\""), lastReply("body"));
        await(() -> card().count() == 0);
        assertEquals(1, replyCount(), "second click must not send another reply");
        assertFalse(session.page().locator("#send").isEnabled(), "run is still active");

        emit("state", "{\"state\":\"succeeded\"}");
        assertTrue(session.page().locator("#send").isEnabled());
    }

    @Test
    void shouldSendReject() {
        open();
        asked("per_1", "shell", "[\"echo hi\"]");

        session.page().locator(".permission-reject").click();
        await(() -> replyCount() == 1);

        assertTrue(lastReply("body").contains("\"decision\":\"reject\""), lastReply("body"));
        await(() -> card().count() == 0);
    }

    @Test
    void shouldCloseTheCardWhenAnsweredElsewhere() {
        open();
        asked("per_1", "shell", "[\"echo hi\"]");

        emit("permission.replied",
                "{\"sessionId\":\"ses_1\",\"requestId\":\"per_1\",\"reply\":\"reject\"}");

        assertEquals(0, card().count());
        assertEquals(0, replyCount(), "an answered request must not be sent again");
    }

    @Test
    void shouldShowErrorForStaleRequestAndNotWaitForInput() {
        open();
        session.page().evaluate("window.__fetchStatus = 404");
        asked("per_1", "shell", "[\"echo hi\"]");

        session.page().locator(".permission-allow").click();
        await(() -> {
            Locator error = session.page().locator(".permission-error");
            return error.count() == 1 && !error.isHidden();
        });

        assertTrue(card().locator(".permission-error").textContent().contains("устарел"),
                card().locator(".permission-error").textContent());
        assertTrue(card().locator(".permission-allow").isDisabled());
        assertTrue(card().locator(".permission-reject").isDisabled());
        assertEquals(1, replyCount());
    }

    @Test
    void shouldIgnorePermissionOfAnotherSession() {
        open();

        emit("permission.asked", "{\"sessionId\":\"ses_2\",\"requestId\":\"per_2\","
                + "\"action\":\"shell\",\"resources\":[\"rm -rf /\"]}");

        assertEquals(0, card().count());
    }

    private void open() {
        session.openResource("/e2e/chat-permissions.html");
    }

    private Locator card() {
        return session.page().locator(".permission-card");
    }

    private void asked(String requestId, String action, String resources) {
        emit("permission.asked", "{\"sessionId\":\"ses_1\",\"requestId\":\"" + requestId
                + "\",\"action\":\"" + action + "\",\"resources\":" + resources + "}");
    }

    private void emit(String name, String json) {
        session.page().evaluate("payload => window.__emit(payload.name, payload.data)",
                Map.of("name", name, "data", json));
    }

    private void clickTwice(String selector) {
        session.page().evaluate("selector => { var button = document.querySelector(selector);"
                + " button.click(); button.click(); }", selector);
    }

    private int replyCount() {
        return ((Number) session.page().evaluate("window.__replies.length")).intValue();
    }

    private String lastReply(String field) {
        return (String) session.page().evaluate("field => window.__lastReply && window.__lastReply[field]", field);
    }

    private void await(BooleanSupplier condition) {
        Page page = session.page();
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("condition was not met in 5 seconds");
            }
            page.waitForTimeout(25);
        }
    }
}
