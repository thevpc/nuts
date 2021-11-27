package net.thevpc.nuts.runtime.standalone.util;

import java.util.Map;
import java.util.function.Function;

public class MapToFunction<K, V> implements Function<K, V> {

    private final Map<K, V> converter;

    public MapToFunction(Map<K, V> converter) {
        this.converter = converter;
    }

    @Override
    public V apply(K t) {
        if (converter == null) {
            return null;
        }
        return converter.get(t);
    }
}
