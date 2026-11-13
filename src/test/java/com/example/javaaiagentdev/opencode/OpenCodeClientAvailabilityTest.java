package com.example.javaaiagentdev.opencode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OpenCodeClientAvailabilityTest {

    private final AtomicInteger status = new AtomicInteger(200);

    private HttpServer server;
    private OpenCodeClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        OpenCodeProperties properties = new OpenCodeProperties(
                "http://127.0.0.1:" + server.getAddress().getPort(), "opencode", "secret", "", "opencode-go",
                "deepseek-v4.1-flash");
        client = new OpenCodeClient(properties, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldBeAvailableWhenServiceAnswers() {
        assertTrue(client.isAvailable());
    }

    @Test
    void shouldBeUnavailableWhenServiceFails() {
        status.set(503);

        assertFalse(client.isAvailable());
    }

    @Test
    void shouldBeUnavailableWhenServiceIsDown() {
        server.stop(0);

        assertFalse(client.isAvailable());
    }
}
