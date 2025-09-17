package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategyModel;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitLeakyBucketStrategy implements NRateLimitStrategy {
    private final String id;
    private final Duration duration;
    private final int capacity;
    private final long leakIntervalMillis; // time to produce 1 token
    private double available; // fractional tokens allowed
    private Instant lastRefill;

    public NRateLimitLeakyBucketStrategy(NRateLimitStrategyModel model) {
        this.id = model.getId();
        this.capacity = model.getMax();
        this.duration = model.getDuration();
        this.leakIntervalMillis = model.getDuration().toMillis() / capacity;
        this.available = model.getAvailable();
        this.lastRefill = model.getLastRefill()==null?Instant.now():lastRefill;
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
    public synchronized NRateLimitStrategyModel toModel() {
        // persist capacity, availableTokens (fractional), lastUpdateMillis
        return new NRateLimitStrategyModel(
                id,
                NRateLimitDefaultStrategy.LEAKY_BUCKET.id(),
                capacity,
                duration,
                available,
                lastRefill,
                new byte[0]
        );
    }

}
