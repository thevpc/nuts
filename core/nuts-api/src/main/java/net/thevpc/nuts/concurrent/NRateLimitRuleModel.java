package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NUpletElementBuilder;
import net.thevpc.nuts.util.NGetter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the persisted or transferable state of a rate limiting rule.
 * <p>
 * This model captures both the configuration (capacity, duration, strategy)
 * and the dynamic runtime state (available tokens, last refill time)
 * of an {@link NRateLimitRule}. It enables storage, replication,
 * or synchronization of rate limiter state across systems.
 *
 * @since 0.8.8
 */
public class NRateLimitRuleModel implements Serializable, NDescribable {
    /** Unique identifier for this rate limit rule. */
    private String id;

    /** Identifier of the strategy used (e.g., "token-bucket", "leaky-bucket", "fixed-window"). */
    private String strategy;

    /** Maximum number of tokens that can be accumulated (i.e., the bucket capacity). */
    private int capacity;

    /** Refill duration in milliseconds — defines the rate of token replenishment. */
    private long duration;

    /** Number of currently available tokens at the time of serialization. */
    private double available;

    /** Timestamp (in milliseconds since epoch) of the last token refill event. */
    private long lastRefill;

    /** Optional serialized configuration data for custom or strategy-specific settings. */
    private byte[] config;


    /**
     * Constructs a new rate limit rule model.
     *
     * @param id          unique rule identifier
     * @param strategy    name of the applied rate limiting strategy
     * @param capacity    maximum number of tokens that can be stored
     * @param duration    refill duration in milliseconds
     * @param available   number of currently available tokens
     * @param lastRefill  last refill timestamp in milliseconds
     * @param config      optional binary configuration data
     */
    public NRateLimitRuleModel(String id, String strategy, int capacity, long duration, double available, long lastRefill, byte[] config) {
        this.id = id;
        this.strategy = strategy;
        this.capacity = capacity;
        this.duration = duration;
        this.available = available;
        this.lastRefill = lastRefill;
        this.config = config;
    }

    /** Returns a defensive copy of the serialized configuration. */
    @NGetter
    public byte[] config() {
        return config == null ? new byte[0] : Arrays.copyOf(config, config.length);
    }

    /** Returns the rule identifier. */
    @NGetter
    public String id() {
        return id;
    }

    /** Returns the strategy name. */
    @NGetter
    public String strategy() {
        return strategy;
    }

    /** Returns the capacity (maximum token count). */
    @NGetter
    public int capacity() {
        return capacity;
    }

    /** Returns the duration (refill period in milliseconds). */
    @NGetter
    public long duration() {
        return duration;
    }

    /** Returns the number of currently available tokens. */
    @NGetter
    public double available() {
        return available;
    }

    /** Returns the timestamp of the last refill event (ms since epoch). */
    @NGetter
    public long lastRefill() {
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
                .add("id", id())
                .add("capacity", capacity())
                .add("available", available())
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
