package com.streamcore.session;

import com.streamcore.abr.BitrateLevel;

import java.time.Instant;

/**
 * Represents an active viewer session.
 */
public class ViewerSession {
    private String sessionId;
    private Instant connectedAt;
    private BitrateLevel lastBitrateLevel;
    private int bandwidthKbps;

    public ViewerSession() {}

    public ViewerSession(String sessionId, Instant connectedAt) {
        this.sessionId = sessionId;
        this.connectedAt = connectedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public BitrateLevel getLastBitrateLevel() {
        return lastBitrateLevel;
    }

    public void setLastBitrateLevel(BitrateLevel lastBitrateLevel) {
        this.lastBitrateLevel = lastBitrateLevel;
    }

    public int getBandwidthKbps() {
        return bandwidthKbps;
    }

    public void setBandwidthKbps(int bandwidthKbps) {
        this.bandwidthKbps = bandwidthKbps;
    }
}
