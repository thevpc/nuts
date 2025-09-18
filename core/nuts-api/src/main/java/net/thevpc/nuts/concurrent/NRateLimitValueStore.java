package net.thevpc.nuts.concurrent;

public interface NRateLimitValueStore {
    NRateLimitValueModel load(String id);

    void save(NRateLimitValueModel model);
}
