package com.streamcore.cache;

import com.streamcore.abr.BitrateLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ChunkMetadataCacheTest {

    private ChunkMetadataCache cache;

    @BeforeEach
    void setUp() {
        // Create cache with small capacity of 2 items for testing LRU eviction
        cache = new ChunkMetadataCache(2);
        ReflectionTestUtils.setField(cache, "ttlSeconds", 300L);
    }

    @Test
    @DisplayName("Unit: Put and get chunk metadata from cache")
    void testPutAndGet() {
        ChunkMetadataCache.ChunkMetadata chunk = new ChunkMetadataCache.ChunkMetadata(
                "chunk_1", BitrateLevel.HIGH, 1024000L, Instant.now()
        );

        cache.put("chunk_1", chunk);
        ChunkMetadataCache.ChunkMetadata retrieved = cache.get("chunk_1");

        assertNotNull(retrieved, "Retrieved chunk should not be null");
        assertEquals("chunk_1", retrieved.chunkId());
        assertEquals(BitrateLevel.HIGH, retrieved.bitrateLevel());
        assertEquals(1024000L, retrieved.sizeBytes());
    }

    @Test
    @DisplayName("Unit: Missing chunk returns null and calculates cache hit rate")
    void testCacheMissAndHitRate() {
        assertNull(cache.get("non_existent_chunk"));
        assertEquals(0.0, cache.getCacheHitRate());

        ChunkMetadataCache.ChunkMetadata chunk = new ChunkMetadataCache.ChunkMetadata(
                "chunk_2", BitrateLevel.MEDIUM, 512000L, Instant.now()
        );
        cache.put("chunk_2", chunk);

        // 1 hit
        cache.get("chunk_2");

        // 1 hit out of 2 total requests = 0.50 (50%)
        assertEquals(0.50, cache.getCacheHitRate(), 0.001);
    }

    @Test
    @DisplayName("Unit: LRU eviction discards the oldest entry when capacity is exceeded")
    void testLruEviction() {
        ChunkMetadataCache.ChunkMetadata chunk1 = new ChunkMetadataCache.ChunkMetadata(
                "c1", BitrateLevel.LOW, 100L, Instant.now()
        );
        ChunkMetadataCache.ChunkMetadata chunk2 = new ChunkMetadataCache.ChunkMetadata(
                "c2", BitrateLevel.MEDIUM, 200L, Instant.now()
        );
        ChunkMetadataCache.ChunkMetadata chunk3 = new ChunkMetadataCache.ChunkMetadata(
                "c3", BitrateLevel.HIGH, 300L, Instant.now()
        );

        cache.put("c1", chunk1);
        cache.put("c2", chunk2);

        // Access c1 so c2 becomes the least recently used
        cache.get("c1");

        // Putting 3rd chunk should evict c2
        cache.put("c3", chunk3);

        assertNotNull(cache.get("c1"), "c1 was recently accessed, should remain in cache");
        assertNotNull(cache.get("c3"), "c3 was newly added, should exist in cache");
        assertNull(cache.get("c2"), "c2 was least recently used, should be evicted");
    }
}
