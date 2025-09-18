package net.thevpc.nuts.concurrent;

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
