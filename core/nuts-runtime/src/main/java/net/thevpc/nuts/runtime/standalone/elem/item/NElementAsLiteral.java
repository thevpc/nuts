package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NElementAsLiteral implements NLiteral {
    private AbstractNElement elem;

    public NElementAsLiteral(AbstractNElement elem) {
        this.elem = elem;
    }

    @Override
    public NOptional<String> asStringAt(int index) {
        return asLiteralAt(index).asString();
    }

    @Override
    public NOptional<Long> asLongAt(int index) {
        return asLiteralAt(index).asLong();
    }

    @Override
    public NOptional<Integer> asIntAt(int index) {
        return asLiteralAt(index).asInt();
    }

    @Override
    public NOptional<Double> asDoubleAt(int index) {
        return asLiteralAt(index).asDouble();
    }

    @Override
    public boolean isNullAt(int index) {
        return asLiteralAt(index).isNull();
    }

    @Override
    public NLiteral asLiteralAt(int index) {
        return NLiteral.of(asObjectAt(index).orNull());
    }

    @Override
    public String toStringLiteral() {
        return toString();
    }

    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return elem.asPrimitive().flatMap(x -> x.asLiteral().asType(expectedType));
    }

    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        return elem.asPrimitive().flatMap(x -> x.asLiteral().asType(expectedType));
    }

    @Override
    public NOptional<Object> asObject() {
        return NOptional.of(elem);
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Byte> asByte() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Double> asDouble() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Float> asFloat() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Instant> asInstant() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> asLocalDate() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> asLocalTime() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asLocalTime);
    }

    @Override
    public NOptional<NBigComplex> asBigComplex() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asBigComplex);
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asDoubleComplex);
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplex() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asFloatComplex);
    }

    @Override
    public NOptional<Integer> asInt() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> asLong() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Short> asShort() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Character> asChar() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asChar);
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        NOptional<NLiteral> p = elem.asPrimitive().map(NElement::asLiteral);
        if (p.isPresent()) {
            return p.get().isSupportedType(type);
        }
        return false;
    }

    @Override
    public boolean isInstant() {
        return elem.type() == NElementType.INSTANT;
    }

    @Override
    public NOptional<String> asString() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Number> asNumber() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return elem.asPrimitive().map(NElement::asLiteral).flatMap(NLiteral::asBigDecimal);
    }

    @Override
    public boolean isBigDecimal() {
        return elem.type() == NElementType.BIG_DECIMAL;
    }

    @Override
    public boolean isBigInt() {
        return elem.type() == NElementType.BIG_INT;
    }


    @Override
    public boolean isNull() {
        NElementType t = elem.type();
        return t == NElementType.NULL;
    }

    @Override
    public boolean isString() {
        NElementType t = elem.type();
        return t.isAnyString();
    }

    @Override
    public boolean isByte() {
        return elem.type() == NElementType.BYTE;
    }

    @Override
    public boolean isInt() {
        NElementType t = elem.type();
        return t == NElementType.INT;
    }

    @Override
    public boolean isLong() {
        return elem.type() == NElementType.LONG;
    }

    @Override
    public boolean isShort() {
        return elem.type() == NElementType.SHORT;
    }

    @Override
    public boolean isFloat() {
        return elem.type() == NElementType.FLOAT;
    }

    @Override
    public boolean isDouble() {
        return elem.type() == NElementType.DOUBLE;
    }

    @Override
    public boolean isBoolean() {
        return elem.type() == NElementType.BOOLEAN;
    }

    @Override
    public boolean isDecimalNumber() {
        return elem.type().isAnyDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return elem.type().isAnyBigNumber();
    }

    @Override
    public boolean isComplexNumber() {
        return elem.type().isAnyComplexNumber();
    }

    @Override
    public boolean isTemporal() {
        return elem.type().isAnyTemporal();
    }

    @Override
    public boolean isLocalTemporal() {
        return elem.type().isAnyLocalTemporal();
    }

    @Override
    public boolean isStream() {
        return elem.type().isAnyStream();
    }

    @Override
    public boolean isNumber() {
        return elem.type().isAnyNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        return elem.type().isAnyFloatingNumber();
    }

    @Override
    public boolean isOrdinalNumber() {
        return elem.type().isAnyOrdinalNumber();
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        return elem.asElementAt(index)
                .map(x -> x.asLiteral().asObject().orNull());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isBlank() {
        return false;
    }

}
