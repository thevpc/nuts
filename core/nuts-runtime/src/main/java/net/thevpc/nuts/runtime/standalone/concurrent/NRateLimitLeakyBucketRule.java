package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitRuleModel;
import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitRule;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitLeakyBucketRule implements NRateLimitRule {
    private final String id;
    private final Duration duration;
    private final int capacity;
    private final long leakIntervalMillis; // time to produce 1 token
    private double available; // fractional tokens allowed
    private Instant lastRefill;

    public NRateLimitLeakyBucketRule(NRateLimitRuleModel model) {
        this.id = model.getId();
        this.capacity = model.getCapacity();
        this.leakIntervalMillis = model.getDuration() / capacity;
        this.available = model.getAvailable();

        this.duration = model.getDuration() == 0 ? null : Duration.ofMillis(model.getDuration());
        this.lastRefill = model.getLastRefill() == 0 ? null : Instant.ofEpochMilli(model.getLastRefill());
    }

    @Override
    public synchronized boolean tryConsume(int count) {
        refill();
        if (available >= count) {
            available -= count;
            return true;
        }
        return false;
    }

    @Override
    public long nextAvailableMillis(int count) {
        refill();
        if (available >= count) {
            return 0;
        }
        return (long) Math.ceil((count - available) * leakIntervalMillis);
    }

    private void refill() {
        Instant now = Instant.now();
        long elapsed = Duration.between(lastRefill, now).toMillis();
        double newTokens = elapsed / (double) leakIntervalMillis;
        available = Math.min(capacity, available + newTokens);
        lastRefill = now;
    }

    @Override
    public synchronized NRateLimitRuleModel toModel() {
        // persist capacity, availableTokens (fractional), lastUpdateMillis
        return new NRateLimitRuleModel(
                id,
                NRateLimitDefaultStrategy.LEAKY_BUCKET.id(),
                capacity,
                duration==null?0:duration.toMillis(),
                available,
                lastRefill == null ? 0 : lastRefill.toEpochMilli(),
                new byte[0]
        );
    }

    @Override
    public String toString() {
        return "LeakyBuket{" +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                '}';
    }
}
