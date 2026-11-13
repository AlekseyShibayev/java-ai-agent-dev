package com.example.javaaiagentdev.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.readonly-datasource")
public record ReadOnlyDataSourceProperties(
        String url,
        String username,
        String password) {
}
