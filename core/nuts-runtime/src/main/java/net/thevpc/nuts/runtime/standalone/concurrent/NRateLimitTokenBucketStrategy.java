package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategyModel;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitTokenBucketStrategy implements NRateLimitStrategy {
    private String id;
    private int max;
    private Duration duration;
    private int available;
    private Instant lastRefill;

    public NRateLimitTokenBucketStrategy(NRateLimitStrategyModel model) {
        this.id = model.getId();
        this.max = model.getMax();
        this.duration = model.getDuration();
        this.available = (int)model.getAvailable();
        this.lastRefill = model.getLastRefill() != null ? model.getLastRefill() : Instant.now();
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
        double msPerToken = (double) periodMs / max;
        long elapsed = Duration.between(lastRefill, Instant.now()).toMillis();
        return Math.max((long)(neededTokens * msPerToken - elapsed), 0);
    }

    public synchronized void refill() {
        long periodMs = duration.toMillis();
        long elapsed = Duration.between(lastRefill, Instant.now()).toMillis();
        if (elapsed >= periodMs) {
            long cycles = elapsed / periodMs;
            available = Math.min(max, available + (int)(cycles * max));
            lastRefill = lastRefill.plusMillis(cycles * periodMs);
        }
    }

    @Override
    public synchronized NRateLimitStrategyModel toModel() {
        return new NRateLimitStrategyModel(id,
                NRateLimitDefaultStrategy.BUCKET.id(),
                max, duration, available, lastRefill,new byte[0]);
    }
}
