package com.streamcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the StreamCore application.
 * Initializes the Spring Boot context.
 */
@SpringBootApplication
@EnableScheduling
public class StreamCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamCoreApplication.class, args);
    }
}
