package net.thevpc.nuts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public interface NutsValue extends NutsBlankable{
    static NutsValue of(Object any){
        return new DefaultNutsValue(any);
    }

    Object getRaw();

    NutsOptional<Instant> asInstant();

    NutsOptional<Number> asNumber();

    NutsOptional<Boolean> asBoolean();

    NutsOptional<Long> asLong();

    NutsOptional<Double> asDouble();

    NutsOptional<Float> asFloat();

    NutsOptional<Byte> asByte();

    NutsOptional<Short> asShort();

    NutsOptional<Integer> asInt();

    NutsOptional<String> asString();

    NutsOptional<BigInteger> asBigInt();

    NutsOptional<BigDecimal> asBigDecimal();

    boolean isBoolean();

    boolean isNull();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type string
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type string
     */
    boolean isString();

    boolean isByte();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isFloat();

    boolean isDouble();

    boolean isInstant();

    boolean isEmpty();

    boolean isBlank();

    boolean isNumber();
}
