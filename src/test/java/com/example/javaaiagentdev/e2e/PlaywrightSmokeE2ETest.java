package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlaywrightSmokeE2ETest extends PlaywrightE2ETest {

    @Test
    void shouldLaunchBrowser() {
        session.openResource("/e2e/fixture.html");

        assertEquals("Chat fixture", session.page().title());
        assertTrue(session.page().locator("[data-testid=chat-input]").isVisible());
    }
}
