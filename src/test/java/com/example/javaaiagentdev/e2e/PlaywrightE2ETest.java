package com.example.javaaiagentdev.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
abstract class PlaywrightE2ETest {

    @RegisterExtension
    final PlaywrightExtension browser = new PlaywrightExtension();

    protected PlaywrightSession session;

    @BeforeEach
    void startBrowser() {
        session = browser.session();
    }
}
