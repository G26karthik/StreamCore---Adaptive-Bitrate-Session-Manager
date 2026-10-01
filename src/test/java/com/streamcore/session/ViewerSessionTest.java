package com.streamcore.session;

import com.streamcore.abr.BitrateLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ViewerSessionTest {

    @Test
    @DisplayName("Unit: Verify session initialization and getters/setters")
    void testSessionInitialization() {
        Instant now = Instant.now();
        ViewerSession session = new ViewerSession("sess_001", now);

        assertEquals("sess_001", session.getSessionId());
        assertEquals(now, session.getConnectedAt());
        assertNull(session.getLastBitrateLevel());
        assertEquals(0, session.getBandwidthKbps());
    }

    @Test
    @DisplayName("Unit: Verify updating session bitrate and bandwidth telemetry")
    void testUpdateSessionMetrics() {
        ViewerSession session = new ViewerSession("sess_002", Instant.now());
        session.setBandwidthKbps(4500);
        session.setLastBitrateLevel(BitrateLevel.HIGH);

        assertEquals(4500, session.getBandwidthKbps());
        assertEquals(BitrateLevel.HIGH, session.getLastBitrateLevel());
    }
}
