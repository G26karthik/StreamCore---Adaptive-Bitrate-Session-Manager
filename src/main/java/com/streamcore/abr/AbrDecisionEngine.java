package com.streamcore.abr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * ABR core logic to evaluate optimal bitrate levels based on bandwidth.
 */
@Service
public class AbrDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(AbrDecisionEngine.class);
    private static final String REDIS_HYS_KEY_PREFIX = "streamcore:hysteresis:";

    private final StringRedisTemplate redisTemplate;

    public AbrDecisionEngine(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Determines proper bitrate level considering hysteresis.
     * Switch down immediately, switch up after 3 consecutive qualified checks.
     */
    public BitrateLevel decide(int bandwidthKbps, String sessionId, BitrateLevel currentLevel) {
        BitrateLevel rawTarget = evaluateRaw(bandwidthKbps);

        if (currentLevel == null) {
            return rawTarget;
        }

        if (rawTarget.ordinal() < currentLevel.ordinal()) {
            // Switch DOWN immediately
            resetHysteresis(sessionId);
            return rawTarget;
        } else if (rawTarget.ordinal() > currentLevel.ordinal()) {
            // Switch UP logic with hysteresis
            long consecutiveCount = incrementHysteresis(sessionId);
            if (consecutiveCount >= 3) {
                resetHysteresis(sessionId);
                return rawTarget;
            } else {
                return currentLevel; // Delay switch up
            }
        }

        // Same level
        resetHysteresis(sessionId);
        return currentLevel;
    }

    private BitrateLevel evaluateRaw(int bandwidthKbps) {
        if (bandwidthKbps < 1000) return BitrateLevel.LOW;
        if (bandwidthKbps < 3000) return BitrateLevel.MEDIUM;
        if (bandwidthKbps < 8000) return BitrateLevel.HIGH;
        return BitrateLevel.ULTRA;
    }

    private long incrementHysteresis(String sessionId) {
        String key = REDIS_HYS_KEY_PREFIX + sessionId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
             redisTemplate.expire(key, Duration.ofMinutes(5));
        }
        return count == null ? 0 : count;
    }

    private void resetHysteresis(String sessionId) {
        redisTemplate.delete(REDIS_HYS_KEY_PREFIX + sessionId);
    }
}
