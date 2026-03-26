package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.NElementAutoUndestructable;

import java.io.Serializable;

/**
 * classes implementing this interfaces MUST extend java.lang.Number
 */
public interface NNumber extends Serializable, NElementAutoUndestructable {
    int intValue();

    long longValue();

    float floatValue();

    double doubleValue();

    byte byteValue();

    short shortValue();

    /**
     * must return THIS instance
     *
     * @return
     */
    Number numberValue();
}
