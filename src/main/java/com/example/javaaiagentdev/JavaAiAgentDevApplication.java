package com.example.javaaiagentdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class JavaAiAgentDevApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaAiAgentDevApplication.class, args);
    }
}
