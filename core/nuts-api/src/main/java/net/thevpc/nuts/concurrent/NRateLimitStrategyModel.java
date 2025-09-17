package net.thevpc.nuts.concurrent;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class NRateLimitStrategyModel implements Serializable {
    private String id;
    private String strategy;
    private int max;
    private Duration duration;
    private double available;
    private Instant lastRefill;
    private byte[] config;

    public NRateLimitStrategyModel(String id, String strategy, int max, Duration duration, double available, Instant lastRefill, byte[] config) {
        this.id = id;
        this.strategy = strategy;
        this.max = max;
        this.duration = duration;
        this.available = available;
        this.lastRefill = lastRefill;
        this.config = config;
    }

    public byte[] getConfig() {
        return config == null ? new byte[0] : Arrays.copyOf(config, config.length);
    }

    public String getId() {
        return id;
    }

    public String getStrategy() {
        return strategy;
    }

    public int getMax() {
        return max;
    }

    public Duration getDuration() {
        return duration;
    }

    public double getAvailable() {
        return available;
    }

    public Instant getLastRefill() {
        return lastRefill;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitStrategyModel that = (NRateLimitStrategyModel) o;
        return max == that.max && available == that.available
                && Objects.equals(id, that.id)
                && Objects.equals(duration, that.duration)
                && Objects.equals(strategy, that.strategy)
                && Objects.equals(lastRefill, that.lastRefill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, duration, available, lastRefill);
    }

    @Override
    public String toString() {
        return "NRateLimitStrategyModel{" +
                "id=" + id +
                ", max=" + max +
                ", strategy=" + strategy +
                ", duration=" + duration +
                ", available=" + available +
                ", lastRefill=" + lastRefill +
                '}';
    }
}
