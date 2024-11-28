package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NPrimitiveElement;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public interface NLiteral extends NBlankable {

    static NLiteral of(Object any) {
        return DefaultNLiteral.of(any);
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

    NOptional<Character> asChar();

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

    NOptional<String> asStringAt(int index);

    NOptional<Long> asLongAt(int index);

    NOptional<Integer> asIntAt(int index);

    NOptional<Double> asDoubleAt(int index);

    boolean isNullAt(int index);

    NLiteral asLiteralAt(int index);

    NOptional<Object> asObjectAt(int index);

    boolean isBlank();

    boolean isNumber();

    boolean isSupportedType(Class<?> type);

    <ET> NOptional<ET> asType(Class<ET> expectedType);

    <ET> NOptional<ET> asType(Type expectedType);

}
