package com.example.javaaiagentdev.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

/**
 * The chat page in a real browser against the running application. OpenCode is replaced by a
 * stub, so no provider, no Ollama and no service password are needed.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ChatPageE2ETest.StubModelConfig.class)
class ChatPageE2ETest extends PlaywrightE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private StubOpenCodeClient model;

    @Test
    void shouldOpenChatPage() {
        open();

        assertThat(session.page().locator("#message")).isVisible();
        assertThat(session.page().locator("#send")).isEnabled();
        assertThat(session.page().locator("#stop")).isDisabled();
        assertThat(session.page().locator("#unavailable")).isHidden();
    }

    @Test
    void shouldShowSentMessageInThread() {
        open();

        send("привет");

        assertThat(session.page().locator(".message-user")).hasText("привет");
        assertThat(session.page().locator("#message")).isDisabled();
        assertThat(session.page().locator("#stop")).isEnabled();
    }

    @Test
    void shouldShowAnswerWhileItIsGenerating() {
        open();
        send("привет");
        awaitRunStarted();

        model.emitTextDelta("При");

        assertThat(session.page().locator(".answer-text")).hasText("При");
        assertEquals("При", session.page().locator(".answer-text").textContent());

        model.emitTextDelta("вет");
        model.emitTextEnded("Привет");
        model.emitSucceeded();

        assertThat(session.page().locator(".answer-text")).hasText("Привет");
    }

    @Test
    void shouldReturnComposerToIdleOnStop() {
        open();
        send("привет");
        assertThat(session.page().locator("#message")).isDisabled();
        int aborts = model.abortCount();

        session.page().locator("#stop").click();

        assertThat(session.page().locator("#message")).isEnabled();
        assertThat(session.page().locator("#send")).isEnabled();
        assertThat(session.page().locator("#stop")).isDisabled();
        assertEquals(aborts + 1, model.abortCount());
    }

    private void open() {
        session.page().navigate("http://127.0.0.1:" + port + "/chat");
    }

    private void send(String text) {
        session.page().locator("#message").fill(text);
        session.page().locator("#send").click();
    }

    /**
     * The answer block is rendered from the response of POST /chat/messages, so once it is attached
     * the run exists and its session is known. A delta emitted earlier would carry the session id of
     * a previous run and be published to no run at all.
     */
    private void awaitRunStarted() {
        session.page().waitForSelector(".answer-text",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class StubModelConfig {

        @Bean
        @Primary
        StubOpenCodeClient stubOpenCodeClient() {
            return new StubOpenCodeClient();
        }
    }
}
