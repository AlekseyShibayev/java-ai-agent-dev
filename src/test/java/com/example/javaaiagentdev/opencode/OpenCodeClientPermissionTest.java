package com.example.javaaiagentdev.opencode;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OpenCodeClientPermissionTest {

    private volatile int status = 404;

    private HttpServer server;
    private OpenCodeClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(status, -1);
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
    void shouldReportUnknownPermissionRequest() {
        assertThrows(NoSuchElementException.class, () -> client.replyPermission("ses_1", "per_old", "allow"));
    }

    @Test
    void shouldReportUnavailableService() {
        status = 500;

        assertThrows(OpenCodeUnavailableException.class, () -> client.replyPermission("ses_1", "per_1", "allow"));
    }
}
