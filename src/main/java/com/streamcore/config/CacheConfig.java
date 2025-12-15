package com.streamcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cache Configuration properties binding class.
 */
@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheConfig {
    private int maxSize = 1000;
    private int ttlSeconds = 300;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
