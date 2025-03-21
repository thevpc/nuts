package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public abstract class AbstractNListContainerElement extends AbstractNElement implements NListContainerElement {
    public AbstractNListContainerElement(NElementType type, NElementAnnotation[] annotations, NElementComments comments) {
        super(type, annotations, comments);
    }

    @Override
    public NOptional<String> getStringByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Integer> getIntByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> getLongByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Float> getFloatByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Double> getDoubleByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Boolean> getBooleanByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Byte> getByteByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Short> getShortByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Instant> getInstantByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalTime> getLocalDateByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asLocalTime);
    }

    @Override
    public NOptional<LocalDate> getLocalTimeByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<BigInteger> getBigIntByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asBigDecimal);
    }

    @Override
    public NOptional<Number> getNumberByPath(String... keys) {
        return getByPath(keys).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<NArrayElement> getArray(String key) {
        return get(key).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NArrayElement> getArray(NElement key) {
        return get(key).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObject(String key) {
        return get(key).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NObjectElement> getObject(NElement key) {
        return get(key).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NListContainerElement> getListContainer(String key) {
        return get(key).flatMap(NElement::asListContainer);
    }

    @Override
    public NOptional<NListContainerElement> getListContainer(NElement key) {
        return get(key).flatMap(NElement::asListContainer);
    }

    @Override
    public NOptional<String> getString(String key) {
        return get(key).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<String> getString(NElement key) {
        return get(key).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Boolean> getBoolean(String key) {
        return get(key).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Boolean> getBoolean(NElement key) {
        return get(key).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Number> getNumber(String key) {
        return get(key).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Number> getNumber(NElement key) {
        return get(key).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Byte> getByte(String key) {
        return get(key).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Byte> getByte(NElement key) {
        return get(key).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Integer> getInt(String key) {
        return get(key).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Integer> getInt(NElement key) {
        return get(key).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> getLong(String key) {
        return get(key).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Long> getLong(NElement key) {
        return get(key).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Short> getShort(String key) {
        return get(key).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Short> getShort(NElement key) {
        return get(key).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Instant> getInstant(String key) {
        return get(key).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDate(String key) {
        return get(key).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTime(String key) {
        return get(key).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTime(String key) {
        return get(key).flatMap(NLiteral::asLocalTime);
    }

    @Override
    public NOptional<Instant> getInstant(NElement key) {
        return get(key).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDate(NElement key) {
        return get(key).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTime(NElement key) {
        return get(key).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTime(NElement key) {
        return get(key).flatMap(NLiteral::asLocalTime);
    }


    @Override
    public NOptional<Float> getFloat(String key) {
        return get(key).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Float> getFloat(NElement key) {
        return get(key).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Double> getDouble(String key) {
        return get(key).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Double> getDouble(NElement key) {
        return get(key).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<BigInteger> getBigInt(NElement key) {
        return get(key).flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimal(NElement key) {
        return get(key).flatMap(NLiteral::asBigDecimal);
    }

    @Override
    public NOptional<NElement> getByPath(String... keys) {
        NOptional<NElement> r = NOptional.of(this);
        for (String key : keys) {
            r=r.flatMap(NElement::asListContainer).flatMap(x->x.get(key));
        }
        return r;
    }

    @Override
    public NOptional<NArrayElement> getArrayByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObjectByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NListContainerElement> getListContainerByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asListContainer);
    }
}
