package net.thevpc.nuts.util;

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
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

    static NOptional<Object> ofObject(Object any) {
        return of(any).asObject();
    }

    static NOptional<Instant> ofInstant(Object any) {
        return of(any).asInstant();
    }

    static NOptional<LocalDate> ofLocalDate(Object any) {
        return of(any).asLocalDate();
    }

    static NOptional<LocalDateTime> ofLocalDateTime(Object any) {
        return of(any).asLocalDateTime();
    }

    static NOptional<LocalTime> ofLocalTime(Object any) {
        return of(any).asLocalTime();
    }

    static NOptional<NBigComplex> ofBigComplex(Object any) {
        return of(any).asBigComplex();
    }

    static NOptional<NDoubleComplex> ofDoubleComplex(Object any) {
        return of(any).asDoubleComplex();
    }

    static NOptional<NFloatComplex> ofFloatComplex(Object any) {
        return of(any).asFloatComplex();
    }

    static NOptional<Number> ofNumber(Object any) {
        return of(any).asNumber();
    }

    static NOptional<Boolean> ofBoolean(Object any) {
        return of(any).asBoolean();
    }

    static NOptional<Long> ofLong(Object any) {
        return of(any).asLong();
    }

    static NOptional<Double> ofDouble(Object any) {
        return of(any).asDouble();
    }

    static NOptional<Float> ofFloat(Object any) {
        return of(any).asFloat();
    }

    static NOptional<Byte> ofByte(Object any) {
        return of(any).asByte();
    }

    static NOptional<Short> ofShort(Object any) {
        return of(any).asShort();
    }

    static NOptional<Character> ofChar(Object any) {
        return of(any).asChar();
    }

    static NOptional<Integer> ofInt(Object any) {
        return of(any).asInt();
    }

    static NOptional<String> ofString(Object any) {
        return of(any).asString();
    }

    static NOptional<BigInteger> ofBigInt(Object any) {
        return of(any).asBigInt();
    }

    static NOptional<BigDecimal> ofBigDecimal(Object any) {
        return of(any).asBigDecimal();
    }

    NOptional<Object> asObject();

    NOptional<Instant> asInstant();

    NOptional<LocalDate> asLocalDate();

    NOptional<LocalDateTime> asLocalDateTime();

    NOptional<LocalTime> asLocalTime();

    NOptional<NBigComplex> asBigComplex();

    NOptional<NDoubleComplex> asDoubleComplex();

    NOptional<NFloatComplex> asFloatComplex();

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

    NOptional<String> asStringAt(int index);

    NOptional<Long> asLongAt(int index);

    NOptional<Integer> asIntAt(int index);

    NOptional<Double> asDoubleAt(int index);

    boolean isNullAt(int index);

    NLiteral asLiteralAt(int index);

    NOptional<Object> asObjectAt(int index);

    boolean isBlank();

    boolean isNumber();

    boolean isOrdinalNumber();

    boolean isFloatingNumber();

    boolean isSupportedType(Class<?> type);

    <ET> NOptional<ET> asType(Class<ET> expectedType);

    <ET> NOptional<ET> asType(Type expectedType);

}
