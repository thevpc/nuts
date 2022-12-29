package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public abstract class AbstractNNavigatableElement extends AbstractNElement implements NNavigatableElement {
    public AbstractNNavigatableElement(NElementType type, NSession session) {
        super(type, session);
    }
    @Override
    public NOptional<String> getStringByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asString);
    }

    @Override
    public NOptional<Integer> getIntByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asInt);
    }

    @Override
    public NOptional<Long> getLongByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asLong);
    }

    @Override
    public NOptional<Float> getFloatByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asFloat);
    }

    @Override
    public NOptional<Double> getDoubleByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asDouble);
    }

    @Override
    public NOptional<Boolean> getBooleanByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asBoolean);
    }

    @Override
    public NOptional<Byte> getByteByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asByte);
    }

    @Override
    public NOptional<Short> getShortByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asShort);
    }

    @Override
    public NOptional<Instant> getInstantByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asInstant);
    }

    @Override
    public NOptional<BigInteger> getBigIntByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asBigDecimal);
    }

    @Override
    public NOptional<Number> getNumberByPath(String... keys) {
        return getByPath(keys).flatMap(NValue::asNumber);
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
    public NOptional<NNavigatableElement> getNavigatable(String key) {
        return get(key).flatMap(NElement::asNavigatable);
    }

    @Override
    public NOptional<NNavigatableElement> getNavigatable(NElement key) {
        return get(key).flatMap(NElement::asNavigatable);
    }

    @Override
    public NOptional<String> getString(String key) {
        return get(key).flatMap(NValue::asString);
    }

    @Override
    public NOptional<String> getString(NElement key) {
        return get(key).flatMap(NValue::asString);
    }

    @Override
    public NOptional<Boolean> getBoolean(String key) {
        return get(key).flatMap(NValue::asBoolean);
    }

    @Override
    public NOptional<Boolean> getBoolean(NElement key) {
        return get(key).flatMap(NValue::asBoolean);
    }

    @Override
    public NOptional<Number> getNumber(String key) {
        return get(key).flatMap(NValue::asNumber);
    }

    @Override
    public NOptional<Number> getNumber(NElement key) {
        return get(key).flatMap(NValue::asNumber);
    }

    @Override
    public NOptional<Byte> getByte(String key) {
        return get(key).flatMap(NValue::asByte);
    }

    @Override
    public NOptional<Byte> getByte(NElement key) {
        return get(key).flatMap(NValue::asByte);
    }

    @Override
    public NOptional<Integer> getInt(String key) {
        return get(key).flatMap(NValue::asInt);
    }

    @Override
    public NOptional<Integer> getInt(NElement key) {
        return get(key).flatMap(NValue::asInt);
    }

    @Override
    public NOptional<Long> getLong(String key) {
        return get(key).flatMap(NValue::asLong);
    }

    @Override
    public NOptional<Long> getLong(NElement key) {
        return get(key).flatMap(NValue::asLong);
    }

    @Override
    public NOptional<Short> getShort(String key) {
        return get(key).flatMap(NValue::asShort);
    }

    @Override
    public NOptional<Short> getShort(NElement key) {
        return get(key).flatMap(NValue::asShort);
    }

    @Override
    public NOptional<Instant> getInstant(String key) {
        return get(key).flatMap(NValue::asInstant);
    }

    @Override
    public NOptional<Instant> getInstant(NElement key) {
        return get(key).flatMap(NValue::asInstant);
    }

    @Override
    public NOptional<Float> getFloat(String key) {
        return get(key).flatMap(NValue::asFloat);
    }

    @Override
    public NOptional<Float> getFloat(NElement key) {
        return get(key).flatMap(NValue::asFloat);
    }

    @Override
    public NOptional<Double> getDouble(String key) {
        return get(key).flatMap(NValue::asDouble);
    }

    @Override
    public NOptional<Double> getDouble(NElement key) {
        return get(key).flatMap(NValue::asDouble);
    }

    @Override
    public NOptional<BigInteger> getBigInt(NElement key) {
        return get(key).flatMap(NValue::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimal(NElement key) {
        return get(key).flatMap(NValue::asBigDecimal);
    }

    @Override
    public NOptional<NElement> getByPath(String... keys) {
        NOptional<NElement> r = NOptional.of(this);
        for (String key : keys) {
            r=r.flatMap(NElement::asNavigatable).flatMap(x->x.get(key));
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
    public NOptional<NNavigatableElement> getNavigatableByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asNavigatable);
    }
}
