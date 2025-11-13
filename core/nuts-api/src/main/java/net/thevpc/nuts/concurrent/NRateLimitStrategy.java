package net.thevpc.nuts.concurrent;

/**
 * A strategy for creating rate limit rules from their persisted models.
 * Implementations define how a particular rate-limiting algorithm
 * (for example, token bucket or fixed window) is reconstructed from
 * its serialized state.
 *
 * @since 0.8.7
 */
public interface NRateLimitStrategy {

    /**
     * Reconstructs a {@link NRateLimitRule} instance from the given
     * {@link NRateLimitRuleModel}.
     *
     * @param model the serialized rule model, not {@code null}
     * @return a new {@link NRateLimitRule} corresponding to the provided model
     */
    NRateLimitRule fromModel(NRateLimitRuleModel model);
}
