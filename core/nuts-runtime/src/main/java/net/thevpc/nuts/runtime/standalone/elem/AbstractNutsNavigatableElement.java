package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public abstract class AbstractNutsNavigatableElement extends AbstractNutsElement implements NutsNavigatableElement {
    public AbstractNutsNavigatableElement(NutsElementType type, NutsSession session) {
        super(type, session);
    }
    @Override
    public NutsOptional<String> getStringByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asString);
    }

    @Override
    public NutsOptional<Integer> getIntByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asInt);
    }

    @Override
    public NutsOptional<Long> getLongByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asLong);
    }

    @Override
    public NutsOptional<Float> getFloatByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asFloat);
    }

    @Override
    public NutsOptional<Double> getDoubleByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asDouble);
    }

    @Override
    public NutsOptional<Boolean> getBooleanByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asBoolean);
    }

    @Override
    public NutsOptional<Byte> getByteByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asByte);
    }

    @Override
    public NutsOptional<Short> getShortByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asShort);
    }

    @Override
    public NutsOptional<Instant> getInstantByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asInstant);
    }

    @Override
    public NutsOptional<BigInteger> getBigIntByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asBigInt);
    }

    @Override
    public NutsOptional<BigDecimal> getBigDecimalByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asBigDecimal);
    }

    @Override
    public NutsOptional<Number> getNumberByPath(String... keys) {
        return getByPath(keys).flatMap(NutsValue::asNumber);
    }
    @Override
    public NutsOptional<NutsArrayElement> getArray(String key) {
        return get(key).flatMap(NutsElement::asArray);
    }

    @Override
    public NutsOptional<NutsArrayElement> getArray(NutsElement key) {
        return get(key).flatMap(NutsElement::asArray);
    }

    @Override
    public NutsOptional<NutsObjectElement> getObject(String key) {
        return get(key).flatMap(NutsElement::asObject);
    }

    @Override
    public NutsOptional<NutsObjectElement> getObject(NutsElement key) {
        return get(key).flatMap(NutsElement::asObject);
    }

    @Override
    public NutsOptional<NutsNavigatableElement> getNavigatable(String key) {
        return get(key).flatMap(NutsElement::asNavigatable);
    }

    @Override
    public NutsOptional<NutsNavigatableElement> getNavigatable(NutsElement key) {
        return get(key).flatMap(NutsElement::asNavigatable);
    }

    @Override
    public NutsOptional<String> getString(String key) {
        return get(key).flatMap(NutsValue::asString);
    }

    @Override
    public NutsOptional<String> getString(NutsElement key) {
        return get(key).flatMap(NutsValue::asString);
    }

    @Override
    public NutsOptional<Boolean> getBoolean(String key) {
        return get(key).flatMap(NutsValue::asBoolean);
    }

    @Override
    public NutsOptional<Boolean> getBoolean(NutsElement key) {
        return get(key).flatMap(NutsValue::asBoolean);
    }

    @Override
    public NutsOptional<Number> getNumber(String key) {
        return get(key).flatMap(NutsValue::asNumber);
    }

    @Override
    public NutsOptional<Number> getNumber(NutsElement key) {
        return get(key).flatMap(NutsValue::asNumber);
    }

    @Override
    public NutsOptional<Byte> getByte(String key) {
        return get(key).flatMap(NutsValue::asByte);
    }

    @Override
    public NutsOptional<Byte> getByte(NutsElement key) {
        return get(key).flatMap(NutsValue::asByte);
    }

    @Override
    public NutsOptional<Integer> getInt(String key) {
        return get(key).flatMap(NutsValue::asInt);
    }

    @Override
    public NutsOptional<Integer> getInt(NutsElement key) {
        return get(key).flatMap(NutsValue::asInt);
    }

    @Override
    public NutsOptional<Long> getLong(String key) {
        return get(key).flatMap(NutsValue::asLong);
    }

    @Override
    public NutsOptional<Long> getLong(NutsElement key) {
        return get(key).flatMap(NutsValue::asLong);
    }

    @Override
    public NutsOptional<Short> getShort(String key) {
        return get(key).flatMap(NutsValue::asShort);
    }

    @Override
    public NutsOptional<Short> getShort(NutsElement key) {
        return get(key).flatMap(NutsValue::asShort);
    }

    @Override
    public NutsOptional<Instant> getInstant(String key) {
        return get(key).flatMap(NutsValue::asInstant);
    }

    @Override
    public NutsOptional<Instant> getInstant(NutsElement key) {
        return get(key).flatMap(NutsValue::asInstant);
    }

    @Override
    public NutsOptional<Float> getFloat(String key) {
        return get(key).flatMap(NutsValue::asFloat);
    }

    @Override
    public NutsOptional<Float> getFloat(NutsElement key) {
        return get(key).flatMap(NutsValue::asFloat);
    }

    @Override
    public NutsOptional<Double> getDouble(String key) {
        return get(key).flatMap(NutsValue::asDouble);
    }

    @Override
    public NutsOptional<Double> getDouble(NutsElement key) {
        return get(key).flatMap(NutsValue::asDouble);
    }

    @Override
    public NutsOptional<BigInteger> getBigInt(NutsElement key) {
        return get(key).flatMap(NutsValue::asBigInt);
    }

    @Override
    public NutsOptional<BigDecimal> getBigDecimal(NutsElement key) {
        return get(key).flatMap(NutsValue::asBigDecimal);
    }

    @Override
    public NutsOptional<NutsElement> getByPath(String... keys) {
        NutsOptional<NutsElement> r = NutsOptional.of(this);
        for (String key : keys) {
            r=r.flatMap(NutsElement::asNavigatable).flatMap(x->x.get(key));
        }
        return r;
    }

    @Override
    public NutsOptional<NutsArrayElement> getArrayByPath(String... keys) {
        return getByPath(keys).flatMap(NutsElement::asArray);
    }

    @Override
    public NutsOptional<NutsObjectElement> getObjectByPath(String... keys) {
        return getByPath(keys).flatMap(NutsElement::asObject);
    }

    @Override
    public NutsOptional<NutsNavigatableElement> getNavigatableByPath(String... keys) {
        return getByPath(keys).flatMap(NutsElement::asNavigatable);
    }
}
