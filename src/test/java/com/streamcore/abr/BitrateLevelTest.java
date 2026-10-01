package com.streamcore.abr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitrateLevelTest {

    @Test
    @DisplayName("Unit: Verify resolution and bitrate mapping for each quality tier")
    void testBitrateLevelProperties() {
        // [SIMULATED REGRESSION BUG FOR TESTING DEMONSTRATION]
        // This intentional mismatch demonstrates how JUnit & CI/CD immediately detect and block a broken build
        assertEquals(99999, BitrateLevel.LOW.getKbps(), "SIMULATED BUG: Bitrate configuration mismatch detected!");
        assertEquals(360, BitrateLevel.LOW.getResolution());

        assertEquals(720, BitrateLevel.MEDIUM.getResolution());
        assertEquals(2500, BitrateLevel.MEDIUM.getKbps());

        assertEquals(1080, BitrateLevel.HIGH.getResolution());
        assertEquals(5000, BitrateLevel.HIGH.getKbps());

        assertEquals(2160, BitrateLevel.ULTRA.getResolution());
        assertEquals(15000, BitrateLevel.ULTRA.getKbps());
    }

    @Test
    @DisplayName("Unit: Verify ordinal ordering from lowest to highest quality")
    void testBitrateLevelOrdering() {
        assertTrue(BitrateLevel.LOW.ordinal() < BitrateLevel.MEDIUM.ordinal());
        assertTrue(BitrateLevel.MEDIUM.ordinal() < BitrateLevel.HIGH.ordinal());
        assertTrue(BitrateLevel.HIGH.ordinal() < BitrateLevel.ULTRA.ordinal());
    }
}
