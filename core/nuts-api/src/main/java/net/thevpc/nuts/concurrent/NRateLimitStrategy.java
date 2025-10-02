package net.thevpc.nuts.concurrent;

/**
 *
 * @since 0.8.7
 */
public interface NRateLimitStrategy {
    NRateLimitRule fromModel(NRateLimitRuleModel model);
}
