package com.streamcore.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local and remote registry tracking active viewers.
 */
@Component
public class SessionRegistry {

    private final Map<String, ViewerSession> activeSessions = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.session.ttl-seconds:1800}")
    private long sessionTtl;

    private static final String REDIS_KEY_PREFIX = "streamcore:session:";

    public SessionRegistry(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ViewerSession getOrCreate(String sessionId) {
        return activeSessions.computeIfAbsent(sessionId, id -> {
            ViewerSession session = new ViewerSession(id, Instant.now());
            saveToRedis(session);
            return session;
        });
    }

    public void updateSession(ViewerSession session) {
        activeSessions.put(session.getSessionId(), session);
        saveToRedis(session);
    }

    public void remove(String sessionId) {
        activeSessions.remove(sessionId);
        redisTemplate.delete(REDIS_KEY_PREFIX + sessionId);
    }

    public AtomicInteger getActiveSessionCount() {
        return new AtomicInteger(activeSessions.size());
    }

    private void saveToRedis(ViewerSession session) {
        String key = REDIS_KEY_PREFIX + session.getSessionId();
        // Storing as HASH would require mapping fields manually or using HashMapper
        // Based on instructions we can just serialize the session object as a value if using standard keys,
        // but instruction says "Store viewer session state as Redis hash".
        // Let's use HashOps.
        redisTemplate.opsForHash().put(key, "sessionId", session.getSessionId());
        redisTemplate.opsForHash().put(key, "connectedAt", session.getConnectedAt().toString());
        if (session.getLastBitrateLevel() != null) {
            redisTemplate.opsForHash().put(key, "lastBitrateLevel", session.getLastBitrateLevel().name());
        }
        redisTemplate.opsForHash().put(key, "bandwidthKbps", session.getBandwidthKbps());
        redisTemplate.expire(key, Duration.ofSeconds(sessionTtl));
    }
}
