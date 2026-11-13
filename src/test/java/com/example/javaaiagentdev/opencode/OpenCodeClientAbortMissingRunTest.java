package com.example.javaaiagentdev.opencode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OpenCodeClientAbortMissingRunTest {

    private HttpServer server;
    private OpenCodeClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(404, -1);
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
    void shouldIgnoreAbortOfAnAlreadyFinishedRun() {
        assertDoesNotThrow(() -> client.abort("ses_1"));
    }
}
