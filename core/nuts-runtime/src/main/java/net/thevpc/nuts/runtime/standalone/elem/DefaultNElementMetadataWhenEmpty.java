package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementMetadata;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.Map;

public class DefaultNElementMetadataWhenEmpty implements NElementMetadata {
    public static final DefaultNElementMetadataWhenEmpty EMPTY = new DefaultNElementMetadataWhenEmpty();

    public DefaultNElementMetadataWhenEmpty() {
    }

    @Override
    public NElementMetadata with(Object key, Object value) {
        NAssert.requireNamedNonNull(key, "key");
        if (value == null) {
            return this;
        }
        return new DefaultNElementMetadataWhenSingle(key, value);
    }

    @Override
    public Map<Object, Object> toMap() {
        return Collections.emptyMap();
    }

    @Override
    public NOptional<Object> get(Object key) {
        return NOptional.ofNamedEmpty(String.valueOf(key));
    }
}
