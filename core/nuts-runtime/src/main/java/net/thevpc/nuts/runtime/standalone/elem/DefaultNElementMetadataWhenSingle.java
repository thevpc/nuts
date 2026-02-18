package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementMetadata;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultNElementMetadataWhenSingle implements NElementMetadata {
    private Object key;
    private Object value;

    public DefaultNElementMetadataWhenSingle(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public NElementMetadata with(Object key, Object value) {
        NAssert.requireNamedNonNull(key, "key");
        if (this.key.equals(key)) {
            if (Objects.equals(this.value, value)) {
                return this;
            } else {
                if (value == null) {
                    return DefaultNElementMetadataWhenEmpty.EMPTY;
                }
                return new DefaultNElementMetadataWhenSingle(key, value);
            }
        }
        if (value == null) {
            return this;
        }
        HashMap<Object, Object> hm = new HashMap<>();
        hm.put(this.key, this.value);
        hm.put(key, value);
        return new DefaultNElementMetadata(Collections.unmodifiableMap(hm));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNElementMetadataWhenSingle that = (DefaultNElementMetadataWhenSingle) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public NOptional<Object> get(Object key) {
        if (this.key.equals(key)) {
            return NOptional.of(value);
        }
        return NOptional.ofNamedEmpty(String.valueOf(key));
    }


    @Override
    public Map<Object, Object> toMap() {
        return NMaps.of(key, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "{" +
                key +
                ", " + value +
                '}';
    }
}
