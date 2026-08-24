package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

/**
 * NElementSeparator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementSeparator extends NAffix {
    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NElementSeparator of(String value) {
        return NElementRPI.of().createSeparator(value);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementSeparator of() {
        return NElementRPI.of().createSeparator(',');
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NElementSeparator of(char value) {
        return NElementRPI.of().createSeparator(value);
    }

    /**
     * Value.
     *
     * @return value result
     */
    String value();
}
