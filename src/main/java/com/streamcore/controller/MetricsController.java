package com.streamcore.controller;

import com.streamcore.cache.ChunkMetadataCache;
import com.streamcore.metrics.StreamMetrics;
import com.streamcore.session.SessionRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Exposes core system metrics without authentication.
 */
@RestController
public class MetricsController {

    private final SessionRegistry sessionRegistry;
    private final ChunkMetadataCache metadataCache;
    private final StreamMetrics metrics;

    public MetricsController(SessionRegistry sessionRegistry, ChunkMetadataCache metadataCache, StreamMetrics metrics) {
        this.sessionRegistry = sessionRegistry;
        this.metadataCache = metadataCache;
        this.metrics = metrics;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeSessions", sessionRegistry.getActiveSessionCount().get());
        response.put("cacheHitRate", metadataCache.getCacheHitRate());

        Timer timer = metrics.getRequestLatencyTimer();
        if (timer != null && timer.takeSnapshot() != null) {
            var snapshot = timer.takeSnapshot();
            long totalRequests = timer.count();
            response.put("totalRequestsHandled", totalRequests);

            double p50 = 0.0;
            double p95 = 0.0;

            var percentiles = snapshot.percentileValues();
            for (var p : percentiles) {
                if (p.percentile() == 0.5) {
                    p50 = p.value(TimeUnit.MILLISECONDS);
                } else if (p.percentile() == 0.95) {
                    p95 = p.value(TimeUnit.MILLISECONDS);
                }
            }

            response.put("p50LatencyMs", p50);
            response.put("p95LatencyMs", p95);
        } else {
            response.put("totalRequestsHandled", 0);
            response.put("p50LatencyMs", 0.0);
            response.put("p95LatencyMs", 0.0);
        }

        return response;
    }
}
