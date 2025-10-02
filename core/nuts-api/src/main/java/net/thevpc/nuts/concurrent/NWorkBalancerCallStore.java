package net.thevpc.nuts.concurrent;

public interface NWorkBalancerCallStore {
    NWorkBalancerCallModel load(String id);

    void save(NWorkBalancerCallModel model);
}
