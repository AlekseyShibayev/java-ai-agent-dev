package com.example.javaaiagentdev.opencode;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "opencode", name = "spike", havingValue = "true")
public class OpenCodeSpike implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenCodeSpike.class);

    private final OpenCodeClient client;

    public OpenCodeSpike(OpenCodeClient client) {
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) {
        String sessionId = client.createSession("spike " + Instant.now());
        log.info("OpenCode session created: {}", sessionId);
        client.prompt(sessionId, "Reply with exactly: ok");
        client.waitForCompletion(sessionId);
        log.info("OpenCode spike response: {}", client.lastAssistantText(sessionId));
    }
}
