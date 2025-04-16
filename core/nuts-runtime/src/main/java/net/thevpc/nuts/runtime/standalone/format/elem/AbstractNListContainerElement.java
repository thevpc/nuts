package net.thevpc.nuts.runtime.standalone.format.elem;

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
    public NOptional<String> getStringValue(int index) {
        return get(index).flatMap(NElement::asStringValue);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asLocalTime());
    }

    @Override
    public NOptional<NArrayElement> getArray(int index) {
        return get(index).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObject(int index) {
        return get(index).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asBoolean());
    }

    @Override
    public NOptional<Byte> getByteValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asByte());
    }

    @Override
    public NOptional<Short> getShortValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asShort());
    }

    @Override
    public NOptional<Integer> getIntValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asInt());
    }

    @Override
    public NOptional<Long> getLongValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asLong());
    }

    @Override
    public NOptional<Float> getFloatValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asFloat());
    }

    @Override
    public NOptional<Double> getDoubleValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asDouble());
    }

    @Override
    public NOptional<Instant> getInstantValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asInstant());
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asLocalDate());
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(int index) {
        return get(index).flatMap(x->x.asLiteral().asLocalDateTime());
    }

//    @Override
//    public NOptional<String> getStringByPath(String... keys) {
//        return getByPath(keys).map(NElement::asLiteral).flatMap(NLiteral::asString);
//    }
//
//    @Override
//    public NOptional<Integer> getIntByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asInt);
//    }
//
//    @Override
//    public NOptional<Long> getLongByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLong);
//    }
//
//    @Override
//    public NOptional<Float> getFloatByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asFloat);
//    }
//
//    @Override
//    public NOptional<Double> getDoubleByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asDouble);
//    }
//
//    @Override
//    public NOptional<Boolean> getBooleanByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBoolean);
//    }
//
//    @Override
//    public NOptional<Byte> getByteByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asByte);
//    }
//
//    @Override
//    public NOptional<Short> getShortByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asShort);
//    }
//
//    @Override
//    public NOptional<Instant> getInstantByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asInstant);
//    }
//
//    @Override
//    public NOptional<LocalTime> getLocalDateByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalTime);
//    }
//
//    @Override
//    public NOptional<LocalDate> getLocalTimeByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalDate);
//    }
//
//    @Override
//    public NOptional<LocalDateTime> getLocalDateTimeByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalDateTime);
//    }
//
//    @Override
//    public NOptional<BigInteger> getBigIntByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBigInt);
//    }
//
//    @Override
//    public NOptional<BigDecimal> getBigDecimalByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBigDecimal);
//    }
//
//    @Override
//    public NOptional<Number> getNumberByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asNumber);
//    }

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
    public NOptional<String> getStringValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<String> getStringValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Number> getNumber(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Number> getNumber(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Byte> getByteValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Byte> getByteValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Integer> getIntValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Integer> getIntValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> getLongValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Long> getLongValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Short> getShortValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Short> getShortValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Instant> getInstantValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalTime);
    }

    @Override
    public NOptional<Instant> getInstantValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalTime);
    }


    @Override
    public NOptional<Float> getFloatValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Float> getFloatValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Double> getDoubleValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Double> getDoubleValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<BigInteger> getBigIntValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBigDecimal);
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
