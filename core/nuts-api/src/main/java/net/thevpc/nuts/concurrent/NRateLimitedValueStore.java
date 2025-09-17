package net.thevpc.nuts.concurrent;

public interface NRateLimitedValueStore {
    NRateLimitedValueModel load(String id);

    void save(NRateLimitedValueModel model);
}
