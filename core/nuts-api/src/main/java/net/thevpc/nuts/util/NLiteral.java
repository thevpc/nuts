package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NBigComplex;
import net.thevpc.nuts.elem.NDoubleComplex;
import net.thevpc.nuts.elem.NFloatComplex;
import net.thevpc.nuts.elem.NPrimitiveElement;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface NLiteral extends NBlankable {

    static NLiteral of(Object any) {
        return DefaultNLiteral.of(any);
    }

    Object asObjectValue();

    NOptional<Instant> asInstantValue();

    NOptional<LocalDate> asLocalDateValue();

    NOptional<LocalDateTime> asLocalDateTimeValue();

    NOptional<LocalTime> asLocalTimeValue();

    NOptional<NBigComplex> asBigComplexValue();

    NOptional<NDoubleComplex> asDoubleComplexValue();

    NOptional<NFloatComplex> asFloatComplexValue();

    NOptional<Number> asNumberValue();

    NOptional<Boolean> asBooleanValue();

    NOptional<Long> asLongValue();

    NOptional<Double> asDoubleValue();

    NOptional<Float> asFloatValue();

    NOptional<Byte> asByteValue();

    NOptional<Short> asShortValue();

    NOptional<Character> asCharValue();

    NOptional<Integer> asIntValue();

    NOptional<String> asStringValue();

    NOptional<BigInteger> asBigIntValue();

    NOptional<BigDecimal> asBigDecimalValue();

    boolean isStream();

    boolean isBoolean();

    boolean isDecimalNumber();

    boolean isBigNumber();

    boolean isComplexNumber();

    boolean isTemporal();

    boolean isLocalTemporal();

    boolean isBigDecimal();

    boolean isBigInt();

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

    NOptional<String> asStringValueAt(int index);

    NOptional<Long> asLongValueAt(int index);

    NOptional<Integer> asIntValueAt(int index);

    NOptional<Double> asDoubleValueAt(int index);

    boolean isNullAt(int index);

    NLiteral asLiteralAt(int index);

    NOptional<Object> asObjectValueAt(int index);

    boolean isBlank();

    boolean isNumber();

    boolean isSupportedType(Class<?> type);

    <ET> NOptional<ET> asType(Class<ET> expectedType);

    <ET> NOptional<ET> asType(Type expectedType);

}
