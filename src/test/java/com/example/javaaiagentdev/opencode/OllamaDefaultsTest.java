package com.example.javaaiagentdev.opencode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;

/**
 * Дефолты чата — локальная модель Ollama. Читается настоящий `application.yaml` из classpath,
 * поэтому тест ловит и смену дефолта, и потерю переопределения через переменные окружения.
 */
class OllamaDefaultsTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    void shouldUseLocalOllamaModelByDefault() {
        runner.run(context -> {
            OpenCodeProperties properties = context.getBean(OpenCodeProperties.class);
            assertEquals("ollama", properties.provider());
            assertEquals("qwen3.5:4b-q4_K_M", properties.model());
        });
    }

    @Test
    void shouldAllowOverridingProviderAndModel() {
        runner.withPropertyValues("OPENCODE_PROVIDER=opencode-go", "OPENCODE_MODEL=deepseek-v4.1-flash")
                .run(context -> {
                    OpenCodeProperties properties = context.getBean(OpenCodeProperties.class);
                    assertEquals("opencode-go", properties.provider());
                    assertEquals("deepseek-v4.1-flash", properties.model());
                });
    }

    @Configuration
    @EnableConfigurationProperties(OpenCodeProperties.class)
    static class PropertiesConfig {
    }
}
