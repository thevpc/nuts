package net.thevpc.nuts.concurrent;

/**
 * Represents a rate limiting rule that controls how frequently actions
 * can be performed or resources can be consumed within a given time window.
 * <p>
 * Implementations typically use token-bucket, leaky-bucket, or fixed-window
 * algorithms to track and limit the number of permitted operations.
 * Each call to {@link #tryConsume(int)} attempts to consume one or more tokens,
 * representing permission to proceed with an operation.
 *
 * @since 0.8.8
 */
public interface NRateLimitRule {
    /**
     * Try to consume tokens.
     * @param count number of tokens
     * @return true if tokens were available and consumed
     */
    boolean tryConsume(int count);

    /**
     * How long to wait (ms) until enough tokens are available.
     * @param count number of tokens to consume
     * @return milliseconds to wait
     */
    long nextAvailableMillis(int count);

    /**
     * Serialize current state for persistence
     */
    NRateLimitRuleModel toModel();
}
