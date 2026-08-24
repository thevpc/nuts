package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * NElementMetadata interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementMetadata {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementMetadata of() {
        return NElementRPI.of().createElementMetadata();
    }

    /**
     * Creates a new instance of of.
     *
     * @param key key
     * @param value value
     * @return of result
     */
    static NElementMetadata of(Object key, Object value) {
        return NElementRPI.of().createElementMetadata(key, value);
    }

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    static NElementMetadata of(Map<Object, Object> any) {
        return NElementRPI.of().createElementMetadata(any);
    }

    /**
     * With.
     *
     * @param key key
     * @param value value
     * @return with result
     */
    NElementMetadata with(Object key, Object value);

    /**
     * Converts to map.
     *
     * @return to map result
     */
    Map<Object, Object> toMap();

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    NOptional<Object> get(Object key);
}
