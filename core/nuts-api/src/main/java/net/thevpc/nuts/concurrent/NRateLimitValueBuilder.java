package net.thevpc.nuts.concurrent;

import java.time.Duration;

public interface NRateLimitValueBuilder {
    NRateLimitValueBuilder id(String id);

    NRateLimitValue build();

    NRateLimitRuleBuilder withLimit(String limitId);

    default NRateLimitRuleBuilder withLimit(String limitId, int capacity) {
        return withLimit(limitId).withCapacity(capacity);
    }

    default NRateLimitRuleBuilder withLimit(String limitId, int capacity, Duration duration) {
        return withLimit(limitId).withCapacity(capacity).withDuration(duration);
    }

    default NRateLimitRuleBuilder withLimit(int capacity) {
        return withLimit(null).withCapacity(capacity);
    }

    default NRateLimitRuleBuilder withLimit(int capacity, Duration duration) {
        return withLimit(null).withCapacity(capacity).withDuration(duration);
    }

}
