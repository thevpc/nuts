package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

import java.time.Instant;

public interface NRateLimitRuleBuilder {
    NRateLimitRuleBuilder withCapacity(int capacity);

    NRateLimitRuleBuilder withStartDate(Instant startDate);

    NRateLimitRuleBuilder withStrategy(NRateLimitDefaultStrategy strategy);

    NRateLimitRuleBuilder withStrategy(String strategy);

    NRateLimitRuleBuilder withDuration(NDuration duration);

    default NRateLimitRuleBuilder per(NDuration duration) {
        return withDuration(duration);
    }

    default NRateLimitRuleBuilder withLimit(String limitId) {
        return end().withLimit(limitId);
    }

    default NRateLimitRuleBuilder withLimit(String limitId, int max) {
        return end().withLimit(limitId, max);
    }

    default NRateLimitRuleBuilder withLimit(String limitId, int max, String strategy) {
        return end().withLimit(limitId).withCapacity(max).withStrategy(strategy);
    }

    default NRateLimitRuleBuilder withLimit(String limitId, int capacity, NDuration duration) {
        return end().withLimit(limitId).withCapacity(capacity).withDuration(duration);
    }

    default NRateLimitRuleBuilder withLimit(int capacity) {
        return end().withLimit(null).withCapacity(capacity);
    }

    default NRateLimitRuleBuilder withLimit(int capacity, NDuration duration) {
        return end().withLimit(null).withCapacity(capacity).withDuration(duration);
    }

    default NRateLimitValue build() {
        return end().build();
    }

    NRateLimitValueBuilder end();
}
