package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NWorkBalancerStrategy {
    void onStartCall(NWorkBalancerStrategyEvent event);

    void onEndCall(NWorkBalancerStrategyEvent event);

    String selectWorker(NWorkBalancerStrategyContext context);
}
