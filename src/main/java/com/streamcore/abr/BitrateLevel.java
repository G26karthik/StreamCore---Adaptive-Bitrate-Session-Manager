package com.streamcore.abr;

/**
 * Representation of different streaming quality tiers.
 */
public enum BitrateLevel {
    LOW(360, 800),
    MEDIUM(720, 2500),
    HIGH(1080, 5000),
    ULTRA(2160, 15000); // 4K

    private final int resolution;
    private final int kbps;

    BitrateLevel(int resolution, int kbps) {
        this.resolution = resolution;
        this.kbps = kbps;
    }

    public int getResolution() { return resolution; }
    public int getKbps() { return kbps; }
}
