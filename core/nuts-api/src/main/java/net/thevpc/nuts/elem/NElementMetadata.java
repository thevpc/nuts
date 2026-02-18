package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.Map;

public interface NElementMetadata {
    static NElementMetadata of() {
        return NElements.of().createElementMetadata();
    }

    static NElementMetadata of(Object key, Object value) {
        return NElements.of().createElementMetadata(key, value);
    }

    static NElementMetadata of(Map<Object, Object> any) {
        return NElements.of().createElementMetadata(any);
    }

    NElementMetadata with(Object key, Object value);

    Map<Object, Object> toMap();

    NOptional<Object> get(Object key);
}
