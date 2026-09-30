package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;

/**
 * Builder interface for constructing {@link NRateLimitValue} instances.
 * <p>
 * Each {@code NRateLimitValue} may include one or more rate-limit rules
 * defined via {@link #withLimit(String)} or its overloads.
 * The builder allows configuring multiple independent limits,
 * each identified by an optional limit ID.
 *
 * @since 0.8.8
 */
public interface NRateLimitValueBuilder {
    /**
     * Sets the unique identifier of the rate-limited value.
     *
     * @param id identifier to assign; may be {@code null} for anonymous values
     * @return this builder instance for chaining
     */
    NRateLimitValueBuilder id(String id);

    /**
     * Builds a new {@link NRateLimitValue} using the configured limits and parameters.
     *
     * @return a fully constructed {@link NRateLimitValue}
     */
    NRateLimitValue build();

    /**
     * Begins defining a new rate limit rule associated with the given limit ID.
     * <p>
     * Each call to this method starts a new {@link NRateLimitRuleBuilder}
     * to configure capacity, duration, and other rule-specific properties.
     *
     * @param limitId optional limit identifier (may be {@code null})
     * @return a new {@link NRateLimitRuleBuilder} for configuring the rule
     */
    NRateLimitRuleBuilder withLimit(String limitId);

    /**
     * Convenience method to define a rate limit with the given capacity.
     *
     * @param limitId  optional limit identifier
     * @param capacity maximum number of allowed tokens in the bucket
     * @return a {@link NRateLimitRuleBuilder} initialized with the given capacity
     */
    default NRateLimitRuleBuilder withLimit(String limitId, int capacity) {
        return withLimit(limitId).withCapacity(capacity);
    }


    /**
     * Convenience method to define a rate limit with capacity and duration.
     *
     * @param limitId  optional limit identifier
     * @param capacity maximum number of allowed tokens in the bucket
     * @param duration period during which tokens are replenished
     * @return a {@link NRateLimitRuleBuilder} initialized with capacity and duration
     */
    default NRateLimitRuleBuilder withLimit(String limitId, int capacity, NDuration duration) {
        return withLimit(limitId).withCapacity(capacity).withDuration(duration);
    }

    /**
     * Defines an unnamed rate limit rule with the given capacity.
     *
     * @param capacity maximum number of allowed tokens in the bucket
     * @return a {@link NRateLimitRuleBuilder} initialized with the given capacity
     */
    default NRateLimitRuleBuilder withLimit(int capacity) {
        return withLimit(null).withCapacity(capacity);
    }

    /**
     * Defines an unnamed rate limit rule with capacity and duration.
     *
     * @param capacity maximum number of allowed tokens in the bucket
     * @param duration period during which tokens are replenished
     * @return a {@link NRateLimitRuleBuilder} initialized with capacity and duration
     */
    default NRateLimitRuleBuilder withLimit(int capacity, NDuration duration) {
        return withLimit(null).withCapacity(capacity).withDuration(duration);
    }

}
