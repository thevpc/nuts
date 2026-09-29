package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

import java.time.Instant;

/**
 * NRateLimitRuleBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NRateLimitRuleBuilder {
    /**
     * With capacity.
     *
     * @param capacity capacity
     * @return with capacity result
     */
    NRateLimitRuleBuilder withCapacity(int capacity);

    /**
     * With start date.
     *
     * @param startDate start date
     * @return with start date result
     */
    NRateLimitRuleBuilder withStartDate(Instant startDate);

    /**
     * With strategy.
     *
     * @param strategy strategy
     * @return with strategy result
     */
    NRateLimitRuleBuilder withStrategy(NRateLimitDefaultStrategy strategy);

    /**
     * With strategy.
     *
     * @param strategy strategy
     * @return with strategy result
     */
    NRateLimitRuleBuilder withStrategy(String strategy);

    /**
     * With duration.
     *
     * @param duration duration
     * @return with duration result
     */
    NRateLimitRuleBuilder withDuration(NDuration duration);

    /**
     * Per.
     *
     * @param duration duration
     * @return per result
     */
    default NRateLimitRuleBuilder per(NDuration duration) {
        return withDuration(duration);
    }

    /**
     * With limit.
     *
     * @param limitId limit id
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(String limitId) {
        return end().withLimit(limitId);
    }

    /**
     * With limit.
     *
     * @param limitId limit id
     * @param max max
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(String limitId, int max) {
        return end().withLimit(limitId, max);
    }

    /**
     * With limit.
     *
     * @param limitId limit id
     * @param max max
     * @param strategy strategy
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(String limitId, int max, String strategy) {
        return end().withLimit(limitId).withCapacity(max).withStrategy(strategy);
    }

    /**
     * With limit.
     *
     * @param limitId limit id
     * @param capacity capacity
     * @param duration duration
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(String limitId, int capacity, NDuration duration) {
        return end().withLimit(limitId).withCapacity(capacity).withDuration(duration);
    }

    /**
     * With limit.
     *
     * @param capacity capacity
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(int capacity) {
        return end().withLimit(null).withCapacity(capacity);
    }

    /**
     * With limit.
     *
     * @param capacity capacity
     * @param duration duration
     * @return with limit result
     */
    default NRateLimitRuleBuilder withLimit(int capacity, NDuration duration) {
        return end().withLimit(null).withCapacity(capacity).withDuration(duration);
    }

    /**
     * Build.
     *
     * @return build result
     */
    default NRateLimitValue build() {
        return end().build();
    }

    /**
     * End.
     *
     * @return end result
     */
    NRateLimitValueBuilder end();
}
