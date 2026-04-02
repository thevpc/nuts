package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NNormalizedStringMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NNormalizedStringMapImpl<T> extends AbstractMap<String, T> implements NNormalizedStringMap<T> {
    private final Map<String, Map.Entry<String, T>> map = new HashMap<>();
    private final Function<String, String> normalizer;
    public static <T> NNormalizedStringMapImpl<T> ofCaseInsensitive(){
        return new NNormalizedStringMapImpl<>(a -> (a == null ? null : a.toLowerCase()));
    }
    public static <T> NNormalizedStringMapImpl<T> ofFormatInsensitive(){
        return new NNormalizedStringMapImpl<>(a -> (a == null ? null : NNameFormat.ID_NAME.format(a)));
    }
    public NNormalizedStringMapImpl(Function<String, String> normalizer) {
        this.normalizer = NAssert.requireNamedNonNull(normalizer, "normalizer");
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return map.entrySet().stream().map(x -> new SimpleImmutableEntry<String, T>(x.getValue().getKey(), x.getValue().getValue())).collect(Collectors.toSet());
    }

    @Override
    public T get(Object key) {
        Entry<String, T> u = map.get(normalizeKey(key));
        return u == null ? null : u.getValue();
    }

    private String normalizeKey(Object key) {
        return normalizeKey(key == null ? null : key.toString());
    }

    private String normalizeKey(String key) {
        return normalizer.apply(key);
    }

    public T put(String key, T value) {
        Entry<String, T> old = map.put(normalizeKey(key), new SimpleImmutableEntry<>(key, value));
        return old == null ? null : old.getValue();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(normalizeKey(key), value);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(normalizeKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
}
