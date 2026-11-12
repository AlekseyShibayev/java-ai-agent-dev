package com.example.devagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TelegramAiDevAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramAiDevAgentApplication.class, args);
    }
}
