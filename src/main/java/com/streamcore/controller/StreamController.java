package com.streamcore.controller;

import com.streamcore.abr.AbrDecisionEngine;
import com.streamcore.abr.BitrateLevel;
import com.streamcore.cache.ChunkMetadataCache;
import com.streamcore.metrics.StreamMetrics;
import com.streamcore.session.SessionRegistry;
import com.streamcore.session.ViewerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controller bound to WebSocket STOMP paths managing video stream simulations.
 */
@Controller
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);

    private final SessionRegistry sessionRegistry;
    private final AbrDecisionEngine abrEngine;
    private final ChunkMetadataCache metadataCache;
    private final StreamMetrics metrics;
    private final SimpMessagingTemplate messagingTemplate;

    public StreamController(SessionRegistry sessionRegistry, AbrDecisionEngine abrEngine,
                            ChunkMetadataCache metadataCache, StreamMetrics metrics,
                            SimpMessagingTemplate messagingTemplate) {
        this.sessionRegistry = sessionRegistry;
        this.abrEngine = abrEngine;
        this.metadataCache = metadataCache;
        this.metrics = metrics;
        this.messagingTemplate = messagingTemplate;
    }

    public record StreamUpdateMessage(String sessionId, int bandwidthKbps) {}
    public record QualityUpdateMessage(BitrateLevel assignedLevel, String chunkId) {}

    @MessageMapping("/stream.update")
    public void handleStreamUpdate(StreamUpdateMessage message) {
        Instant start = Instant.now();
        
        try {
            ViewerSession session = sessionRegistry.getOrCreate(message.sessionId());
            session.setBandwidthKbps(message.bandwidthKbps());

            BitrateLevel currentLevel = session.getLastBitrateLevel();
            BitrateLevel newLevel = abrEngine.decide(message.bandwidthKbps(), message.sessionId(), currentLevel);

            String mockChunkId = "chunk_" + newLevel.name() + "_time_" + (System.currentTimeMillis() / 10000);
            
            ChunkMetadataCache.ChunkMetadata chunk = metadataCache.get(mockChunkId);
            if (chunk == null) {
                // Simulate fetch delay
                long delayMs = ThreadLocalRandom.current().nextLong(20, 81);
                Thread.sleep(delayMs);
                
                chunk = new ChunkMetadataCache.ChunkMetadata(mockChunkId, newLevel, newLevel.getKbps() * 1024L, Instant.now());
                metadataCache.put(mockChunkId, chunk);
            }

            session.setLastBitrateLevel(newLevel);
            sessionRegistry.updateSession(session);

            messagingTemplate.convertAndSend("/topic/quality-update/" + message.sessionId(), 
                    new QualityUpdateMessage(newLevel, mockChunkId));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Chunk fetch interrupted", e);
        } finally {
            metrics.recordLatency(Duration.between(start, Instant.now()));
        }
    }
}
