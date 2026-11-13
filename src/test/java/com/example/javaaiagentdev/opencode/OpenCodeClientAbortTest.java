package com.example.javaaiagentdev.opencode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OpenCodeClientAbortTest {

    private final List<String> requests = new CopyOnWriteArrayList<>();
    private final List<String> bodies = new CopyOnWriteArrayList<>();

    private HttpServer server;
    private OpenCodeClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
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
    void shouldAbortSession() {
        client.abort("ses_1");

        assertEquals(List.of("POST /api/session/ses_1/interrupt"), requests);
    }

    @Test
    void shouldReplyPermission() {
        client.replyPermission("ses_1", "per_1", "reject");

        assertEquals(List.of("POST /api/session/ses_1/permission/per_1/reply"), requests);
        assertTrue(bodies.get(0).contains("\"decision\":\"reject\""), bodies.get(0));
    }
}
