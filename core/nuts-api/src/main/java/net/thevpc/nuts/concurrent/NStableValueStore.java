package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.6
 */
public interface NStableValueStore {
    NStableValueModel load(String id);

    void save(NStableValueModel value);
}
