package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

public interface NRateLimitValueBuilder {
    NRateLimitValueBuilder id(String id);

    NRateLimitValue build();

    NRateLimitRuleBuilder withLimit(String limitId);

    default NRateLimitRuleBuilder withLimit(String limitId, int capacity) {
        return withLimit(limitId).withCapacity(capacity);
    }

    default NRateLimitRuleBuilder withLimit(String limitId, int capacity, NDuration duration) {
        return withLimit(limitId).withCapacity(capacity).withDuration(duration);
    }

    default NRateLimitRuleBuilder withLimit(int capacity) {
        return withLimit(null).withCapacity(capacity);
    }

    default NRateLimitRuleBuilder withLimit(int capacity, NDuration duration) {
        return withLimit(null).withCapacity(capacity).withDuration(duration);
    }

}
