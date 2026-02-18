package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementMetadata;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultNElementMetadata implements NElementMetadata {
    private Map<Object, Object> unmodifiableMap;
    public static NElementMetadata EMPTY = DefaultNElementMetadataWhenEmpty.EMPTY;

    public static NElementMetadata of() {
        return EMPTY;
    }

    public static NElementMetadata of(Object key, Object value) {
        NAssert.requireNamedNonNull(key, "key");
        if (value == null) {
            return EMPTY;
        }
        return new DefaultNElementMetadataWhenSingle(key, value);
    }

    public static NElementMetadata of(Map<Object, Object> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }
        Map<Object, Object> copy = new HashMap<>();
        Object lastKey = null;
        Object lastValue = null;
        for (Map.Entry<Object, Object> e : values.entrySet()) {
            Object key = e.getKey();
            NAssert.requireNamedNonNull(key, "key");
            Object value = e.getValue();
            if (value != null) {
                copy.put(key, value);
                lastKey = key;
                lastValue = value;
            }
        }
        if (copy.isEmpty()) {
            return EMPTY;
        }
        if (copy.size() == 1) {
            return new DefaultNElementMetadataWhenSingle(lastKey, lastValue);
        }
        return new DefaultNElementMetadata(Collections.unmodifiableMap(copy));
    }

    DefaultNElementMetadata(Map<Object, Object> unmodifiableMap) {
        this.unmodifiableMap = unmodifiableMap;
    }

    @Override
    public NElementMetadata with(Object key, Object value) {
        NAssert.requireNamedNonNull(key, "key");
        Object a = unmodifiableMap.get(key);
        if (Objects.equals(a, value)) {
            return this;
        }
        Map<Object, Object> values2 = new HashMap<>(unmodifiableMap);
        if (value == null) {
            values2.remove(key);
        } else {
            values2.put(key, value);
        }
        if (values2.isEmpty()) {
            return EMPTY;
        }
        if (values2.size() == 1) {
            Map.Entry<Object, Object> entry = values2.entrySet().iterator().next();
            return new DefaultNElementMetadataWhenSingle(entry.getKey(), entry.getValue());
        }
        return new DefaultNElementMetadata(Collections.unmodifiableMap(values2));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNElementMetadata that = (DefaultNElementMetadata) o;
        return Objects.equals(unmodifiableMap, that.unmodifiableMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unmodifiableMap);
    }

    @Override
    public String toString() {
        return unmodifiableMap.toString();
    }

    @Override
    public Map<Object, Object> toMap() {
        return unmodifiableMap;
    }

    @Override
    public NOptional<Object> get(Object key) {
        return NOptional.ofNamed(unmodifiableMap.get(key), String.valueOf(key));
    }
}
