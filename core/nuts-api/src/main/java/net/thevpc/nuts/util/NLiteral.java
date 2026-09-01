package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
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

/**
 * NLiteral interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLiteral extends NBlankable {

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    static NLiteral of(Object any) {
        return NUtilsRPI.of().createLiteral(any);
    }

    /**
     * Creates a new instance of of object.
     *
     * @param any any
     * @return of object result
     */
    static NOptional<Object> ofObject(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asObject( any).as object(
         * @return of result
         */
        return of(any).asObject();
    }

    /**
     * Creates a new instance of of instant.
     *
     * @param any any
     * @return of instant result
     */
    static NOptional<Instant> ofInstant(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asInstant( any).as instant(
         * @return of result
         */
        return of(any).asInstant();
    }

    /**
     * Creates a new instance of of local date.
     *
     * @param any any
     * @return of local date result
     */
    static NOptional<LocalDate> ofLocalDate(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asLocalDate( any).as local date(
         * @return of result
         */
        return of(any).asLocalDate();
    }

    /**
     * Creates a new instance of of local date time.
     *
     * @param any any
     * @return of local date time result
     */
    static NOptional<LocalDateTime> ofLocalDateTime(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asLocalDateTime( any).as local date time(
         * @return of result
         */
        return of(any).asLocalDateTime();
    }

    /**
     * Creates a new instance of of local time.
     *
     * @param any any
     * @return of local time result
     */
    static NOptional<LocalTime> ofLocalTime(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asLocalTime( any).as local time(
         * @return of result
         */
        return of(any).asLocalTime();
    }

    /**
     * Creates a new instance of of big complex.
     *
     * @param any any
     * @return of big complex result
     */
    static NOptional<NBigComplex> ofBigComplex(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asBigComplex( any).as big complex(
         * @return of result
         */
        return of(any).asBigComplex();
    }

    /**
     * Creates a new instance of of double complex.
     *
     * @param any any
     * @return of double complex result
     */
    static NOptional<NDoubleComplex> ofDoubleComplex(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asDoubleComplex( any).as double complex(
         * @return of result
         */
        return of(any).asDoubleComplex();
    }

    /**
     * Creates a new instance of of float complex.
     *
     * @param any any
     * @return of float complex result
     */
    static NOptional<NFloatComplex> ofFloatComplex(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asFloatComplex( any).as float complex(
         * @return of result
         */
        return of(any).asFloatComplex();
    }

    /**
     * Creates a new instance of of number.
     *
     * @param any any
     * @return of number result
     */
    static NOptional<Number> ofNumber(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asNumber( any).as number(
         * @return of result
         */
        return of(any).asNumber();
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @param any any
     * @return of boolean result
     */
    static NOptional<Boolean> ofBoolean(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asBoolean( any).as boolean(
         * @return of result
         */
        return of(any).asBoolean();
    }

    /**
     * Creates a new instance of of long.
     *
     * @param any any
     * @return of long result
     */
    static NOptional<Long> ofLong(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asLong( any).as long(
         * @return of result
         */
        return of(any).asLong();
    }

    /**
     * Creates a new instance of of double.
     *
     * @param any any
     * @return of double result
     */
    static NOptional<Double> ofDouble(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asDouble( any).as double(
         * @return of result
         */
        return of(any).asDouble();
    }

    /**
     * Creates a new instance of of float.
     *
     * @param any any
     * @return of float result
     */
    static NOptional<Float> ofFloat(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asFloat( any).as float(
         * @return of result
         */
        return of(any).asFloat();
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param any any
     * @return of byte result
     */
    static NOptional<Byte> ofByte(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asByte( any).as byte(
         * @return of result
         */
        return of(any).asByte();
    }

    /**
     * Creates a new instance of of short.
     *
     * @param any any
     * @return of short result
     */
    static NOptional<Short> ofShort(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asShort( any).as short(
         * @return of result
         */
        return of(any).asShort();
    }

    /**
     * Creates a new instance of of char.
     *
     * @param any any
     * @return of char result
     */
    static NOptional<Character> ofChar(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asChar( any).as char(
         * @return of result
         */
        return of(any).asChar();
    }

    /**
     * Creates a new instance of of int.
     *
     * @param any any
     * @return of int result
     */
    static NOptional<Integer> ofInt(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asInt( any).as int(
         * @return of result
         */
        return of(any).asInt();
    }

    /**
     * Creates a new instance of of string.
     *
     * @param any any
     * @return of string result
     */
    static NOptional<String> ofString(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asString( any).as string(
         * @return of result
         */
        return of(any).asString();
    }

    /**
     * Creates a new instance of of big int.
     *
     * @param any any
     * @return of big int result
     */
    static NOptional<BigInteger> ofBigInt(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asBigInt( any).as big int(
         * @return of result
         */
        return of(any).asBigInt();
    }

    /**
     * Creates a new instance of of big decimal.
     *
     * @param any any
     * @return of big decimal result
     */
    static NOptional<BigDecimal> ofBigDecimal(Object any) {
        /**
         * Creates a new instance of of.
         *
         * @param any).asBigDecimal( any).as big decimal(
         * @return of result
         */
        return of(any).asBigDecimal();
    }

    /**
     * As object.
     *
     * @return as object result
     */
    NOptional<Object> asObject();

    /**
     * As instant.
     *
     * @return as instant result
     */
    NOptional<Instant> asInstant();

    /**
     * As local date.
     *
     * @return as local date result
     */
    NOptional<LocalDate> asLocalDate();

    /**
     * As local date time.
     *
     * @return as local date time result
     */
    NOptional<LocalDateTime> asLocalDateTime();

    /**
     * As local time.
     *
     * @return as local time result
     */
    NOptional<LocalTime> asLocalTime();

    /**
     * As big complex.
     *
     * @return as big complex result
     */
    NOptional<NBigComplex> asBigComplex();

    /**
     * As double complex.
     *
     * @return as double complex result
     */
    NOptional<NDoubleComplex> asDoubleComplex();

    /**
     * As float complex.
     *
     * @return as float complex result
     */
    NOptional<NFloatComplex> asFloatComplex();

    /**
     * As number.
     *
     * @return as number result
     */
    NOptional<Number> asNumber();

    /**
     * As boolean.
     *
     * @return as boolean result
     */
    NOptional<Boolean> asBoolean();

    /**
     * As long.
     *
     * @return as long result
     */
    NOptional<Long> asLong();

    /**
     * As double.
     *
     * @return as double result
     */
    NOptional<Double> asDouble();

    /**
     * As float.
     *
     * @return as float result
     */
    NOptional<Float> asFloat();

    /**
     * As byte.
     *
     * @return as byte result
     */
    NOptional<Byte> asByte();

    /**
     * As short.
     *
     * @return as short result
     */
    NOptional<Short> asShort();

    /**
     * As char.
     *
     * @return as char result
     */
    NOptional<Character> asChar();

    /**
     * As int.
     *
     * @return as int result
     */
    NOptional<Integer> asInt();

    /**
     * As string.
     *
     * @return as string result
     */
    NOptional<String> asString();

    /**
     * As big int.
     *
     * @return as big int result
     */
    NOptional<BigInteger> asBigInt();

    /**
     * As big decimal.
     *
     * @return as big decimal result
     */
    NOptional<BigDecimal> asBigDecimal();

    /**
     * Checks if is stream.
     *
     * @return is stream result
     */
    boolean isStream();

    /**
     * Checks if is boolean.
     *
     * @return is boolean result
     */
    boolean isBoolean();

    /**
     * Checks if is decimal number.
     *
     * @return is decimal number result
     */
    boolean isDecimalNumber();

    /**
     * Checks if is big number.
     *
     * @return is big number result
     */
    boolean isBigNumber();

    /**
     * Checks if is complex number.
     *
     * @return is complex number result
     */
    boolean isComplexNumber();

    /**
     * Checks if is temporal.
     *
     * @return is temporal result
     */
    boolean isTemporal();

    /**
     * Checks if is local temporal.
     *
     * @return is local temporal result
     */
    boolean isLocalTemporal();

    /**
     * Checks if is big decimal.
     *
     * @return is big decimal result
     */
    boolean isBigDecimal();

    /**
     * Checks if is big int.
     *
     * @return is big int result
     */
    boolean isBigInt();

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    boolean isNull();

    /**
     * return true if this element can be cast to {@link NPrimitiveElement} of type string
     *
     * @return true if this element can be cast to {@link NPrimitiveElement} of type string
     */
    boolean isString();

    /**
     * Checks if is byte.
     *
     * @return is byte result
     */
    boolean isByte();

    /**
     * Checks if is int.
     *
     * @return is int result
     */
    boolean isInt();

    /**
     * Checks if is long.
     *
     * @return is long result
     */
    boolean isLong();

    /**
     * Checks if is short.
     *
     * @return is short result
     */
    boolean isShort();

    /**
     * Checks if is float.
     *
     * @return is float result
     */
    boolean isFloat();

    /**
     * Checks if is double.
     *
     * @return is double result
     */
    boolean isDouble();

    /**
     * Checks if is instant.
     *
     * @return is instant result
     */
    boolean isInstant();

    /**
     * Converts to string literal.
     *
     * @return to string literal result
     */
    String toStringLiteral();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * As string at.
     *
     * @param index index
     * @return as string at result
     */
    NOptional<String> asStringAt(int index);

    /**
     * As long at.
     *
     * @param index index
     * @return as long at result
     */
    NOptional<Long> asLongAt(int index);

    /**
     * As int at.
     *
     * @param index index
     * @return as int at result
     */
    NOptional<Integer> asIntAt(int index);

    /**
     * As double at.
     *
     * @param index index
     * @return as double at result
     */
    NOptional<Double> asDoubleAt(int index);

    /**
     * Checks if is null at.
     *
     * @param index index
     * @return is null at result
     */
    boolean isNullAt(int index);

    /**
     * As literal at.
     *
     * @param index index
     * @return as literal at result
     */
    NLiteral asLiteralAt(int index);

    /**
     * As object at.
     *
     * @param index index
     * @return as object at result
     */
    NOptional<Object> asObjectAt(int index);

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    boolean isBlank();

    /**
     * Checks if is number.
     *
     * @return is number result
     */
    boolean isNumber();

    /**
     * Checks if is ordinal number.
     *
     * @return is ordinal number result
     */
    boolean isOrdinalNumber();

    /**
     * Checks if is floating number.
     *
     * @return is floating number result
     */
    boolean isFloatingNumber();

    /**
     * Checks if is supported type.
     *
     * @param type type
     * @return is supported type result
     */
    boolean isSupportedType(Class<?> type);

    /**
     * As type.
     *
     * @param expectedType expected type
     * @return as type result
     */
    <ET> NOptional<ET> asType(Class<ET> expectedType);

    /**
     * As type.
     *
     * @param expectedType expected type
     * @return as type result
     */
    <ET> NOptional<ET> asType(Type expectedType);

}
