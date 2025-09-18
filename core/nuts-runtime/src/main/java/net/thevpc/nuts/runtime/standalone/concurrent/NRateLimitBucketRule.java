package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitRuleModel;
import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitRule;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitBucketRule implements NRateLimitRule {
    private String id;
    private int capacity;
    private Duration duration;
    private int available;
    private Instant lastRefill;

    public NRateLimitBucketRule(NRateLimitRuleModel model) {
        this.id = model.getId();
        this.capacity = model.getCapacity();
        this.available = (int)model.getAvailable();

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
    public synchronized long nextAvailableMillis(int count) {
        if (available >= count) return 0;
        long periodMs = duration.toMillis();
        long neededTokens = count - available;
        double msPerToken = (double) periodMs / capacity;
        long elapsed = Duration.between(lastRefill, Instant.now()).toMillis();
        return Math.max((long)(neededTokens * msPerToken - elapsed), 0);
    }

    public synchronized void refill() {
        if (lastRefill == null) {
            // First time refill, assume bucket fully available
            lastRefill = Instant.now();
            available = capacity;
            return;
        }
        long periodMs = duration.toMillis();
        long elapsed = Duration.between(lastRefill, Instant.now()).toMillis();
        if (elapsed >= periodMs) {
            long cycles = elapsed / periodMs;
            available = Math.min(capacity, available + (int)(cycles * capacity));
            lastRefill = lastRefill.plusMillis(cycles * periodMs);
        }
    }

    @Override
    public synchronized NRateLimitRuleModel toModel() {
        return new NRateLimitRuleModel(id,
                NRateLimitDefaultStrategy.BUCKET.id(),
                capacity, duration==null?0:duration.toMillis(), available, lastRefill == null ? 0 : lastRefill.toEpochMilli(),new byte[0]);
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                '}';
    }
}
