package net.thevpc.nuts;

import net.thevpc.nuts.elem.NPrimitiveElement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public interface NValue extends NBlankable {

    static NValue of(Object any) {
        return DefaultNValue.of(any);
    }

    Object getRaw();

    NOptional<Instant> asInstant();

    NOptional<Number> asNumber();

    NOptional<Boolean> asBoolean();

    NOptional<Long> asLong();

    NOptional<Double> asDouble();

    NOptional<Float> asFloat();

    NOptional<Byte> asByte();

    NOptional<Short> asShort();

    NOptional<Integer> asInt();

    NOptional<String> asString();

    NOptional<BigInteger> asBigInt();

    NOptional<BigDecimal> asBigDecimal();

    boolean isBoolean();

    boolean isNull();

    /**
     * return true if this element can be cast to {@link NPrimitiveElement} of type string
     *
     * @return true if this element can be cast to {@link NPrimitiveElement} of type string
     */
    boolean isString();

    boolean isByte();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isFloat();

    boolean isDouble();

    boolean isInstant();

    String toStringLiteral();

    boolean isEmpty();

    boolean isBlank();

    boolean isNumber();
}
