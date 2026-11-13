package com.example.javaaiagentdev.opencode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenCodeClient {

    private static final Logger log = LoggerFactory.getLogger(OpenCodeClient.class);

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration WAIT_TIMEOUT = Duration.ofMinutes(3);

    private final OpenCodeProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private volatile String resolvedBaseUrl;

    public OpenCodeClient(OpenCodeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isAvailable() {
        try {
            HttpResponse<String> response = send(request("/api/session?limit=1").GET().build());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            }
            invalidateBaseUrl();
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public String createSession(String title) {
        log.debug("Creating OpenCode session: {}", title);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("model", Map.of("id", model(), "providerID", provider()));
        if (isNotBlank(properties.directory())) {
            body.put("location", Map.of("directory", properties.directory()));
        }

        HttpResponse<String> response = send(request("/api/session")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build());
        requireSuccess(response, "create session");

        JsonNode root = readTree(response.body());
        String sessionId = root.path("data").path("id").asText();
        if (sessionId.isBlank()) {
            throw new IllegalStateException("OpenCode returned no session id: " + response.body());
        }
        log.info("OpenCode session created: {}", sessionId);
        return sessionId;
    }

    public void prompt(String sessionId, String text) {
        prompt(sessionId, text, null);
    }

    public void prompt(String sessionId, String text, String agent) {
        log.debug("Sending prompt to session {} ({} chars)", sessionId, text.length());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        if (isNotBlank(agent)) {
            body.put("agent", agent);
        }
        HttpResponse<String> response = send(request("/api/session/" + sessionId + "/prompt")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build());
        requireSuccess(response, "send prompt");
    }

    public void abort(String sessionId) {
        log.debug("Aborting session {}", sessionId);
        HttpResponse<String> response = send(request("/api/session/" + sessionId + "/abort")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
        requireSuccess(response, "abort session");
    }

    public void replyPermission(String sessionId, String requestId, String decision) {
        log.debug("Replying to permission {} of session {}: {}", requestId, sessionId, decision);
        HttpResponse<String> response = send(request("/api/session/" + sessionId + "/permission/" + requestId + "/reply")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(Map.of("decision", decision))))
                .build());
        requireSuccess(response, "reply to permission");
    }

    /** Opens the shared event stream. The caller owns the stream and must close it. */
    public Stream<String> eventLines() {
        HttpRequest request = request("/api/event").GET().build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                invalidateBaseUrl();
                throw new OpenCodeUnavailableException("OpenCode event stream failed: HTTP " + response.statusCode());
            }
            return new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8)).lines();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenCodeUnavailableException("OpenCode event stream interrupted", e);
        } catch (Exception e) {
            invalidateBaseUrl();
            throw new OpenCodeUnavailableException("OpenCode event stream failed: " + e.getMessage(), e);
        }
    }

    public void waitForCompletion(String sessionId) {
        log.debug("Waiting for session {} to become idle", sessionId);
        HttpResponse<String> response = send(request("/api/experimental/session/" + sessionId + "/wait")
                .timeout(WAIT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new IllegalStateException("OpenCode wait failed: HTTP " + response.statusCode() + " " + response.body());
        }
    }

    public String lastAssistantText(String sessionId) {
        log.debug("Reading messages from session {}", sessionId);
        HttpResponse<String> response = send(request("/api/session/" + sessionId + "/message?limit=20")
                .GET()
                .build());
        requireSuccess(response, "read messages");

        JsonNode messages = readTree(response.body()).path("data");
        String failure = null;
        for (JsonNode message : messages) {
            String type = message.path("type").asText();
            if ("idle".equals(type) && "failed".equals(message.path("outcome").asText())) {
                failure = message.path("error").path("message").asText(null);
            }
            if ("assistant".equals(type)) {
                String text = textOf(message);
                if (!text.isBlank()) {
                    log.debug("Assistant reply received ({} chars)", text.length());
                    return text;
                }
                String error = message.path("error").path("message").asText(null);
                if (error != null) {
                    failure = error;
                }
            }
        }
        throw new IllegalStateException("OpenCode returned no assistant text"
                + (failure == null ? "" : ": " + failure));
    }

    public JsonNode sessionMessages(String sessionId) {
        HttpResponse<String> response = send(request("/api/session/" + sessionId + "/message?limit=50")
                .GET()
                .build());
        requireSuccess(response, "read messages");
        return readTree(response.body()).path("data");
    }

    private String textOf(JsonNode assistantMessage) {
        StringBuilder result = new StringBuilder();
        for (JsonNode part : assistantMessage.path("content")) {
            if ("text".equals(part.path("type").asText())) {
                result.append(part.path("text").asText());
            }
        }
        return result.toString().strip();
    }

    private HttpRequest.Builder request(String path) {
        String credentials = "opencode:" + password();
        String auth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return HttpRequest.newBuilder(URI.create(baseUrl() + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json");
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            invalidateBaseUrl();
            throw new OpenCodeUnavailableException("OpenCode request interrupted", e);
        } catch (Exception e) {
            invalidateBaseUrl();
            throw new OpenCodeUnavailableException("OpenCode request failed: " + e.getMessage(), e);
        }
    }

    private void requireSuccess(HttpResponse<String> response, String operation) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OpenCodeUnavailableException("OpenCode " + operation + " failed: HTTP "
                    + response.statusCode() + " " + response.body());
        }
    }

    private void invalidateBaseUrl() {
        resolvedBaseUrl = null;
    }

    private String baseUrl() {
        String cached = resolvedBaseUrl;
        if (cached != null) {
            return cached;
        }
        String configured = properties.baseUrl();
        if (isNotBlank(configured)) {
            resolvedBaseUrl = trimTrailingSlash(configured);
            return resolvedBaseUrl;
        }
        String binary = isNotBlank(properties.binary()) ? properties.binary() : "opencode";
        try {
            Process process = new ProcessBuilder(binary, "service", "status")
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor(10, TimeUnit.SECONDS);
            String url = output.lines()
                    .map(String::strip)
                    .filter(line -> line.startsWith("http"))
                    .findFirst()
                    .orElse(null);
            if (url != null) {
                resolvedBaseUrl = trimTrailingSlash(url);
                return resolvedBaseUrl;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve OpenCode service URL: " + e.getMessage(), e);
        }
        throw new IllegalStateException("Cannot resolve OpenCode service URL; set opencode.base-url or opencode.binary");
    }

    private String password() {
        String password = properties.password();
        if (!isNotBlank(password)) {
            throw new IllegalStateException("opencode.password is not configured");
        }
        return password;
    }

    private String provider() {
        return isNotBlank(properties.provider()) ? properties.provider() : "opencode-go";
    }

    private String model() {
        return isNotBlank(properties.model()) ? properties.model() : "deepseek-v4.1-flash";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize OpenCode request: " + e.getMessage(), e);
        }
    }

    private JsonNode readTree(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse OpenCode response: " + e.getMessage(), e);
        }
    }

    private static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
