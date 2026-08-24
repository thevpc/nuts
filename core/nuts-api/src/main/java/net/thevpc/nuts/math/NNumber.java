package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.NElementSimple;

import java.io.Serializable;

/**
 * classes implementing this interfaces MUST extend java.lang.Number
 */
public interface NNumber extends Serializable, NElementSimple {
    /**
     * Int value.
     *
     * @return int value result
     */
    int intValue();

    /**
     * Long value.
     *
     * @return long value result
     */
    long longValue();

    /**
     * Float value.
     *
     * @return float value result
     */
    float floatValue();

    /**
     * Double value.
     *
     * @return double value result
     */
    double doubleValue();

    /**
     * Byte value.
     *
     * @return byte value result
     */
    byte byteValue();

    /**
     * Short value.
     *
     * @return short value result
     */
    short shortValue();

    /**
     * must return THIS instance
     *
     * @return
     */
    Number numberValue();
}
