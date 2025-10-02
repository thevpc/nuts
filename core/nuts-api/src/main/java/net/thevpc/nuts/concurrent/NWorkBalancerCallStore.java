package net.thevpc.nuts.concurrent;

/**
 *
 * @since 0.8.7
 */
public interface NWorkBalancerCallStore {
    NWorkBalancerModel load(String id);

    void save(NWorkBalancerModel model);
}
