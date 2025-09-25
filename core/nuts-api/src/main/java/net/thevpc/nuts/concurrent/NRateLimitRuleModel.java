package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NUpletElementBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class NRateLimitRuleModel implements Serializable, NElementDescribable {
    private String id;
    private String strategy;
    private int capacity;
    private long duration;
    private double available;
    private long lastRefill;
    private byte[] config;

    public NRateLimitRuleModel(String id, String strategy, int capacity, long duration, double available, long lastRefill, byte[] config) {
        this.id = id;
        this.strategy = strategy;
        this.capacity = capacity;
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

    public int getCapacity() {
        return capacity;
    }

    public long getDuration() {
        return duration;
    }

    public double getAvailable() {
        return available;
    }

    public long getLastRefill() {
        return lastRefill;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitRuleModel that = (NRateLimitRuleModel) o;
        return capacity == that.capacity && available == that.available
                && Objects.equals(id, that.id)
                && Objects.equals(duration, that.duration)
                && Objects.equals(strategy, that.strategy)
                && Objects.equals(lastRefill, that.lastRefill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacity, duration, available, lastRefill);
    }

    @Override
    public String toString() {
        return "NRateLimitRuleModel{" +
                "id=" + id +
                ", capacity=" + capacity +
                ", strategy=" + strategy +
                ", duration=" + duration +
                ", available=" + available +
                ", lastRefill=" + lastRefill +
                '}';
    }

    @Override
    public NElement describe() {
        NUpletElementBuilder b = NElement.ofUpletBuilder("Rule")
                .add("id", getId())
                .add("capacity", getCapacity())
                .add("available", getAvailable())
                .add("duration", duration)
                .add("strategy", strategy);
        if (lastRefill > 0) {
            b.add("lastRefill", lastRefill);
        }
        if (config != null && config.length > 0) {
            b.add("config", NElement.ofByteArray(config));
        }
        return b.build();
    }
}
