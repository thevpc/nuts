package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitRuleModel;
import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitRule;

import java.time.Duration;
import java.time.Instant;

public class NRateLimitFixedWindowRule implements NRateLimitRule {
    private final String id;
    private final int capacity;
    private final Duration duration;
    private int available;
    private Instant lastRefill;

    public NRateLimitFixedWindowRule(NRateLimitRuleModel model) {
        this.id = model.getId();
        this.capacity = model.getCapacity();
        this.duration = model.getDuration() == 0 ? null : Duration.ofMillis(model.getDuration());
        this.available = (int) model.getAvailable();
        this.lastRefill = model.getLastRefill() == 0 ? null : Instant.ofEpochMilli(model.getLastRefill());
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
        if (available + n <= capacity) {
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
        return (available + n > capacity) ? duration.toMillis() - (elapsed) : 0;
    }

    @Override
    public synchronized NRateLimitRuleModel toModel() {
        return new NRateLimitRuleModel(id,
                NRateLimitDefaultStrategy.FIXED_WINDOW.id(),
                capacity, duration == null ? 0 : duration.toMillis(), available, lastRefill == null ? 0 : lastRefill.toEpochMilli(), new byte[0]
        );
    }
    @Override
    public String toString() {
        return "FixedWindow{" +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                '}';
    }
}
