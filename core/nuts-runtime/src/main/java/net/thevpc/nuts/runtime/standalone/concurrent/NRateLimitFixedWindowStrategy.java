package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategy;
import net.thevpc.nuts.concurrent.NRateLimitStrategyModel;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitFixedWindowStrategy implements NRateLimitStrategy {
    private final String id;
    private final int max;
    private final Duration duration;
    private int available;
    private Instant lastRefill;

    public NRateLimitFixedWindowStrategy(NRateLimitStrategyModel model) {
        this.id = model.getId();
        this.max = model.getMax();
        this.duration = model.getDuration();
        this.available = (int) model.getAvailable();
        this.lastRefill = model.getLastRefill();
    }

    @Override
    public synchronized boolean tryConsume(int n) {
        Instant now = Instant.now();
        long elapsed = Duration.between(lastRefill, now).toMillis();

        if (elapsed >= duration.toMillis()) {
            // Reset window
            available = 0;
            lastRefill = now;
        }
        if (available + n <= max) {
            available += n;
            return true;
        }
        return false;
    }

    @Override
    public synchronized long nextAvailableMillis(int n) {
        Instant now = Instant.now();
        long elapsed = Duration.between(lastRefill, now).toMillis();
        if (elapsed >= duration.toMillis()) {
            return 0; // window reset, available immediately
        }
        return (available + n > max) ? duration.toMillis() - (elapsed) : 0;
    }

    @Override
    public synchronized NRateLimitStrategyModel toModel() {
        return new NRateLimitStrategyModel(id,
                NRateLimitDefaultStrategy.FIXED_WINDOW.id(),
                max, duration, available, lastRefill, new byte[0]
        );
    }
}
