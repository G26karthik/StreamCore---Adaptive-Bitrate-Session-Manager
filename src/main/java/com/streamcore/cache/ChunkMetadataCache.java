package com.streamcore.cache;

import com.streamcore.abr.BitrateLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe LRU Cache for managing simulated chunk fetch details.
 */
@Component
public class ChunkMetadataCache {

    private final Map<String, ChunkMetadata> cache;

    @Value("${app.cache.ttl-seconds:300}")
    private long ttlSeconds;

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public ChunkMetadataCache(@Value("${app.cache.max-size:1000}") int maxSize) {
        // Create LRU cache using LinkedHashMap
        LinkedHashMap<String, ChunkMetadata> lru = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ChunkMetadata> eldest) {
                return size() > maxSize;
            }
        };
        this.cache = Collections.synchronizedMap(lru);
    }

    public ChunkMetadata get(String chunkId) {
        ChunkMetadata meta = cache.get(chunkId);
        if (meta == null) {
            misses.incrementAndGet();
            return null;
        }

        // Check TTL
        if (Instant.now().isAfter(meta.fetchedAt().plusSeconds(ttlSeconds))) {
            cache.remove(chunkId);
            misses.incrementAndGet();
            return null;
        }

        hits.incrementAndGet();
        return meta;
    }

    public void put(String chunkId, ChunkMetadata metadata) {
        cache.put(chunkId, metadata);
    }

    public double getCacheHitRate() {
        long h = hits.get();
        long total = h + misses.get();
        if (total == 0) return 0.0;
        return (double) h / total;
    }

    public record ChunkMetadata(String chunkId, BitrateLevel bitrateLevel, long sizeBytes, Instant fetchedAt) {}
}
