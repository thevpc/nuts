package net.thevpc.nuts.concurrent;

public interface NRateLimitedValueBuilder {
    NRateLimitedValueBuilder id(String id);

    NRateLimitedValue build();

    NRateLimitStrategyBuilder withLimit(String limitId);

    default NRateLimitStrategyBuilder withLimit(String limitId, int max) {
        return withLimit(limitId).withMax(max);
    }

    default NRateLimitStrategyBuilder withLimit(String limitId, int max, NRateLimitDefaultStrategy strategy) {
        return withLimit(limitId).withMax(max).withStrategy(strategy);
    }
}
