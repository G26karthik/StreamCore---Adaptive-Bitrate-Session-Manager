package com.streamcore.abr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AbrDecisionEngineTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AbrDecisionEngine engine;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        engine = new AbrDecisionEngine(redisTemplate);
    }

    @Test
    @DisplayName("Unit: Cold start chooses bitrate directly without hysteresis")
    void testColdStartEvaluation() {
        // Cold start (currentLevel == null)
        assertEquals(BitrateLevel.LOW, engine.decide(800, "session_1", null));
        assertEquals(BitrateLevel.MEDIUM, engine.decide(2000, "session_1", null));
        assertEquals(BitrateLevel.HIGH, engine.decide(5000, "session_1", null));
        assertEquals(BitrateLevel.ULTRA, engine.decide(12000, "session_1", null));
    }

    @Test
    @DisplayName("Unit: Switch DOWN immediately when bandwidth drops to avoid buffering")
    void testImmediateDownswitch() {
        // Current is HIGH (1080p), bandwidth suddenly drops to 500 Kbps (LOW)
        BitrateLevel result = engine.decide(500, "session_2", BitrateLevel.HIGH);

        assertEquals(BitrateLevel.LOW, result, "Should drop down to LOW immediately");
        verify(redisTemplate).delete("streamcore:hysteresis:session_2");
    }

    @Test
    @DisplayName("Unit: Switch UP requires 3 consecutive qualified checks (hysteresis dampening)")
    void testHysteresisUpswitchDelay() {
        // Current is LOW, bandwidth surges to 6000 Kbps (target HIGH)
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // 1st spike -> remains LOW
        BitrateLevel firstCheck = engine.decide(6000, "session_3", BitrateLevel.LOW);
        assertEquals(BitrateLevel.LOW, firstCheck, "First spike should stay on LOW due to hysteresis");

        // 2nd spike -> remains LOW
        when(valueOperations.increment(anyString())).thenReturn(2L);
        BitrateLevel secondCheck = engine.decide(6000, "session_3", BitrateLevel.LOW);
        assertEquals(BitrateLevel.LOW, secondCheck, "Second check should still delay upgrade");

        // 3rd consecutive check -> upgrades to HIGH
        when(valueOperations.increment(anyString())).thenReturn(3L);
        BitrateLevel thirdCheck = engine.decide(6000, "session_3", BitrateLevel.LOW);
        assertEquals(BitrateLevel.HIGH, thirdCheck, "Third consecutive check should safely upgrade to HIGH");
    }
}
