package net.thevpc.nuts.concurrent;

import java.time.Duration;
import java.time.Instant;

public interface NRateLimitStrategyBuilder {
    NRateLimitStrategyBuilder withMax(int max);

    NRateLimitStrategyBuilder withStartDate(Instant startDate);
    NRateLimitStrategyBuilder withStrategy(NRateLimitDefaultStrategy strategy);

    NRateLimitStrategyBuilder withStrategy(String strategy);

    NRateLimitStrategyBuilder withDuration(Duration duration);

    default NRateLimitStrategyBuilder per(Duration duration) {
        return withDuration(duration);
    }

    default NRateLimitStrategyBuilder withLimit(String limitId) {
        return end().withLimit(limitId);
    }

    default NRateLimitStrategyBuilder withLimit(String limitId, int max) {
        return end().withLimit(limitId, max);
    }

    default NRateLimitStrategyBuilder withLimit(String limitId, int max, NRateLimitDefaultStrategy strategy) {
        return end().withLimit(limitId).withMax(max).withStrategy(strategy);
    }

    default NRateLimitStrategyBuilder withLimit(String limitId, int max, String strategy) {
        return end().withLimit(limitId).withMax(max).withStrategy(strategy);
    }

    default NRateLimitedValue build() {
        return end().build();
    }

    NRateLimitedValueBuilder end();
}
