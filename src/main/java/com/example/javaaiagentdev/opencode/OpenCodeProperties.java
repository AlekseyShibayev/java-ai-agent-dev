package com.example.javaaiagentdev.opencode;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("opencode")
public record OpenCodeProperties(
        String baseUrl,
        String binary,
        String password,
        String directory,
        String provider,
        String model) {
}
