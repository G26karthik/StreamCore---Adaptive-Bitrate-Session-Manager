package com.streamcore.metrics;

import com.streamcore.cache.ChunkMetadataCache;
import com.streamcore.session.SessionRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * Central metrics collection tying into Micrometer.
 */
@Component
public class StreamMetrics {

    private final MeterRegistry meterRegistry;
    private final SessionRegistry sessionRegistry;
    private final ChunkMetadataCache cache;
    private Timer requestLatencyTimer;

    public StreamMetrics(MeterRegistry meterRegistry, SessionRegistry sessionRegistry, ChunkMetadataCache cache) {
        this.meterRegistry = meterRegistry;
        this.sessionRegistry = sessionRegistry;
        this.cache = cache;
    }

    @PostConstruct
    public void init() {
        Gauge.builder("streamcore.sessions.active", sessionRegistry, sr -> sr.getActiveSessionCount().get())
                .description("Active viewer sessions")
                .register(meterRegistry);

        Gauge.builder("streamcore.cache.hit.rate", cache, ChunkMetadataCache::getCacheHitRate)
                .description("Cache hit rate for chunks")
                .register(meterRegistry);

        this.requestLatencyTimer = Timer.builder("streamcore.request.latency")
                .description("Latency of websocket requests")
                .publishPercentiles(0.5, 0.95)
                .register(meterRegistry);
    }

    public void recordLatency(Duration duration) {
        requestLatencyTimer.record(duration);
    }

    public Timer getRequestLatencyTimer() {
        return requestLatencyTimer;
    }
}
