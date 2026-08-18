package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

public interface NElementMetadata {
    static NElementMetadata of() {
        return NElementRPI.of().createElementMetadata();
    }

    static NElementMetadata of(Object key, Object value) {
        return NElementRPI.of().createElementMetadata(key, value);
    }

    static NElementMetadata of(Map<Object, Object> any) {
        return NElementRPI.of().createElementMetadata(any);
    }

    NElementMetadata with(Object key, Object value);

    Map<Object, Object> toMap();

    NOptional<Object> get(Object key);
}
