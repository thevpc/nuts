package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

import java.util.Map;

public interface NSignatureMap<S extends NSignature<T, ?>, T, V> {
    void putMulti(S sig, V value, S... sigs);

    void put(S sig, V value);

    NOptional<V> get(S sig);

    void remove(S sig);

    Map<S, V> toMap();

    int size();
}
