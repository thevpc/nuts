package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.runtime.standalone.format.elem.builder.*;
import net.thevpc.nuts.runtime.standalone.format.elem.item.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNElementFactory implements NElementFactory {

    public DefaultNElementFactory() {
    }

    @Override
    public NPairElement ofPair(NElement key, NElement value) {
        return new DefaultNPairElement(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value,
                new NElementAnnotation[0], null
        );
    }

    @Override
    public NPairElement ofPair(String key, NElement value) {
        return ofPair(ofNameOrString(key), value);
    }

    @Override
    public NPairElement ofPair(String key, Boolean value) {
        return ofPair(ofNameOrString(key), ofBoolean(value));
    }

    @Override
    public NPairElement ofPair(String key, Short value) {
        return ofPair(ofNameOrString(key), ofShort(value));
    }

    @Override
    public NPairElement ofPair(String key, Byte value) {
        return ofPair(ofNameOrString(key), ofByte(value));
    }

    @Override
    public NPairElement ofPair(String key, Integer value) {
        return ofPair(ofNameOrString(key), ofInt(value));
    }

    @Override
    public NPairElement ofPair(String key, Long value) {
        return ofPair(ofNameOrString(key), ofLong(value));
    }

    @Override
    public NPairElement ofPair(String key, String value) {
        return ofPair(ofNameOrString(key), ofString(value));
    }

    @Override
    public NPairElement ofPair(String key, Double value) {
        return ofPair(ofNameOrString(key), ofDouble(value));
    }

    @Override
    public NPairElement ofPair(String key, Instant value) {
        return ofPair(ofNameOrString(key), ofInstant(value));
    }

    @Override
    public NPairElement ofPair(String key, LocalDate value) {
        return ofPair(ofNameOrString(key), ofLocalDate(value));
    }

    @Override
    public NPairElement ofPair(String key, LocalDateTime value) {
        return ofPair(ofNameOrString(key), ofLocalDateTime(value));
    }

    @Override
    public NPairElement ofPair(String key, LocalTime value) {
        return ofPair(ofNameOrString(key), ofLocalTime(value));
    }

    @Override
    public NPairElementBuilder ofPairBuilder(NElement key, NElement value) {
        return new DefaultNPairElementBuilder(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value
        );
    }

    @Override
    public NOperatorElementBuilder ofOpBuilder() {
        return new DefaultNOperatorElementBuilder();
    }


    @Override
    public NOperatorElement ofOp(NElementType op, NOperatorType type, NElement first, NElement second) {
        return ofOpBuilder().operator(op).operatorType(type).first(first).second(second).build();
    }

    @Override
    public NOperatorElement ofOp(NElementType op, NElement first, NElement second) {
        return ofOp(op,null, first, second);
    }

    @Override
    public NOperatorElement ofOp(NElementType op, NElement operand) {
        return ofOp(op,null, operand, null);
    }

    @Override
    public NPairElementBuilder ofPairBuilder() {
        return new DefaultNPairElementBuilder();
    }

    //    @Override
//    public NutsPrimitiveElementBuilder forPrimitive() {
//        return new DefaultNPrimitiveElementBuilder(session);
//    }
    @Override
    public NObjectElementBuilder ofObjectBuilder() {
        return new DefaultNObjectElementBuilder();
    }

    @Override
    public NObjectElementBuilder ofObjectBuilder(String name) {
        return ofObjectBuilder().name(name);
    }

    @Override
    public NArrayElementBuilder ofArrayBuilder() {
        return new DefaultNArrayElementBuilder();
    }

    @Override
    public NArrayElementBuilder ofArrayBuilder(String name) {
        return ofArrayBuilder().name(name);
    }

    @Override
    public NArrayElement ofArray() {
        return ofArrayBuilder().build();
    }


    @Override
    public NArrayElement ofStringArray(String... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofString).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofDoubleArray(double... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::ofDouble).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofDoubleArray(Double... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofDouble).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofIntArray(int... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::ofInt).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofIntArray(Integer... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofInt).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofLongArray(long... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::ofLong).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofLongArray(Long... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofLong).collect(Collectors.toList())).build();
    }


    @Override
    public NArrayElement ofNumberArray(Number... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofNumber).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofBooleanArray(boolean... items) {
        NArrayElementBuilder b = ofArrayBuilder();
        for (boolean item : items) {
            b.add(item);
        }
        return b.build();
    }

    @Override
    public NArrayElement ofBooleanArray(Boolean... items) {
        return ofArrayBuilder().addAll(Arrays.stream(items).map(this::ofBoolean).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement ofArray(NElement... items) {
        return ofArrayBuilder().addAll(items).build();
    }

    @Override
    public NArrayElement ofArray(String name, NElement... items) {
        return ofArrayBuilder().name(name).addAll(items).build();
    }

    @Override
    public NArrayElement ofNamedArray(String name, NElement... items) {
        return ofArrayBuilder().name(name).addAll(items).build();
    }

    @Override
    public NArrayElement ofNamedParametrizedArray(String name, NElement[] params, NElement... items) {
        return ofArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement ofArray(String name, NElement[] params, NElement... items) {
        return ofArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement ofParametrizedArray(NElement... params) {
        return ofArrayBuilder().addParams(params == null ? null : Arrays.asList(params)).build();
    }


    @Override
    public NArrayElement ofParametrizedArray(NElement[] params, NElement... items) {
        return ofArrayBuilder().addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement ofParametrizedArray(String name, NElement[] params, NElement... items) {
        return ofArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement ofObject(NElement... items) {
        return ofObjectBuilder().addAll(items).build();
    }

    @Override
    public NArrayElement ofParametrizedArray(String name, NElement... params) {
        return ofArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement ofObject(String name, NElement... items) {
        return ofObjectBuilder().name(name).addAll(items).build();
    }

    @Override
    public NObjectElement ofParametrizedObject(NElement[] params, NElement... items) {
        return ofObjectBuilder().addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement ofParametrizedObject(String name, NElement[] params, NElement... items) {
        return ofObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement ofObject(String name, NElement[] params, NElement... items) {
        return ofObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement ofNamedObject(String name, NElement... items) {
        return ofObjectBuilder().name(name).addAll(items).build();
    }

    @Override
    public NObjectElement ofNamedParametrizedObject(String name, NElement[] params, NElement... items) {
        return ofObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement ofParametrizedObject(NElement... params) {
        return ofObjectBuilder().addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement ofParametrizedObject(String name, NElement... params) {
        return ofObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement ofObject() {
        return ofObjectBuilder().build();
    }

    @Override
    public NPrimitiveElement ofBoolean(String value) {
        NOptional<Boolean> o = NLiteral.of(value).asBoolean();
        if (o.isEmpty()) {
            return ofNull();
        }
        return ofBoolean(o.get());
    }

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NPrimitiveElement ofBoolean(boolean value) {
        //TODO: perhaps we can optimize this
        if (value) {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, true, null, null);
        } else {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, false, null, null);
        }
    }

    @Override
    public <T extends Enum<T>> NPrimitiveElement ofEnum(Enum<T> value) {
        if (value == null) {
            return ofNull();
        }
        if (value instanceof NEnum) {
            return ofName(((NEnum) value).id());
        }
        return ofName(value.name());
    }

    public NPrimitiveElement ofString(String str) {
        return ofString(str, null);
    }

    public NPrimitiveElement ofString(String str, NElementType stringLayout) {
        if (str == null) {
            return ofNull();
        }
        if (stringLayout == null) {
            stringLayout = NElementType.DOUBLE_QUOTED_STRING;
        }
        if (stringLayout.isAnyStringOrName()) {
            return new DefaultNStringElement(stringLayout, str, null, null);
        }
        throw new NUnsupportedEnumException(stringLayout);
    }

    public NPrimitiveElement ofRegex(String str) {
        return str == null ? ofNull() : new DefaultNStringElement(NElementType.REGEX, str, null, null);
    }

    public NPrimitiveElement ofName(String str) {
        return str == null ? ofNull() : new DefaultNStringElement(NElementType.NAME, str, null, null);
    }

    @Override
    public NPrimitiveElement ofNameOrString(String value) {
        if (value == null) {
            return ofNull();
        }
        return NElementUtils.isValidElementName(value) ? new DefaultNStringElement(NElementType.NAME, value, null, null)
                : new DefaultNStringElement(NElementType.DOUBLE_QUOTED_STRING, value, null, null)
                ;
    }

    @Override
    public NCustomElement ofCustom(Object object) {
        NAssert.requireNonNull(object, "custom element");
        return new DefaultNCustomElement(object, null, null);
    }

    @Override
    public NPrimitiveElement ofTrue() {
        return ofBoolean(true);
    }

    @Override
    public NPrimitiveElement ofFalse() {
        return ofBoolean(false);
    }

    @Override
    public NPrimitiveElement ofInstant(Instant instant) {
        return instant == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.INSTANT, instant, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalDate(LocalDate localDate) {
        return localDate == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDate, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDateTime, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalTime(LocalTime localTime) {
        return localTime == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_TIME, localTime, null, null);
    }

    @Override
    public NPrimitiveElement ofFloat(Float value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.FLOAT, value);
    }


    @Override
    public NPrimitiveElement ofFloat(float value) {
        return new DefaultNNumberElement(NElementType.FLOAT, value);
    }

    @Override
    public NPrimitiveElement ofFloat(Float value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.FLOAT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofFloat(float value, String suffix) {
        return new DefaultNNumberElement(NElementType.FLOAT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofInt(Integer value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.INT, value);
    }

    @Override
    public NPrimitiveElement ofInt(int value) {
        return new DefaultNNumberElement(NElementType.INT, value, null, null);
    }

    @Override
    public NPrimitiveElement ofInt(Integer value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.INT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofInt(int value, String suffix) {
        return new DefaultNNumberElement(NElementType.INT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofInt(Integer value, NNumberLayout layout) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.INT, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofInt(int value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.INT, value, layout, null);
    }

    @Override
    public NElement ofBinaryStream(NInputStreamProvider value) {
        return value == null ? ofNull() : new DefaultNBinaryStreamElement(value, null, null);
    }

    @Override
    public NBinaryStreamElementBuilder ofBinaryStreamBuilder() {
        return new DefaultNBinaryStreamElementBuilder();
    }

    @Override
    public NElement ofCharStream(NReaderProvider value) {
        return value == null ? ofNull() : new DefaultNCharStreamElement(value, null, null);
    }

    @Override
    public NCharStreamElementBuilder ofCharStreamBuilder() {
        return new DefaultNCharStreamElementBuilder();
    }

    @Override
    public NElementAnnotation ofAnnotation(String name, NElement... values) {
        return new NElementAnnotationImpl(name, values);
    }

    @Override
    public NPrimitiveElement ofLong(Long value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.LONG, value);
    }

    @Override
    public NPrimitiveElement ofLong(long value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.LONG, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofLong(Long value, NNumberLayout layout) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.LONG, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.LONG, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.LONG, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofLong(long value, String suffix) {
        return new DefaultNNumberElement(NElementType.LONG, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofLong(Long value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.LONG, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofLong(long value) {
        return new DefaultNNumberElement(NElementType.LONG, value, null, null);
    }

    @Override
    public NPrimitiveElement ofNull() {
        //perhaps we can optimize this?
        return new DefaultNPrimitiveElement(NElementType.NULL, null, null, null);
    }

    @Override
    public NPrimitiveElement ofNumber(String value) {
        if (value == null) {
            return ofNull();
        }
        if (value.indexOf('.') >= 0) {
            try {
                return ofNumber(Double.parseDouble(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigDecimal(value));
            } catch (Exception ex) {

            }
        } else {
            try {
                return ofNumber(Integer.parseInt(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(Long.parseLong(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigInteger(value));
            } catch (Exception ex) {

            }
        }
        throw new NParseException(NMsg.ofC("unable to parse number %s", value));
    }

    @Override
    public NPrimitiveElement ofInstant(Date value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, value.toInstant(), null, null);
    }

    @Override
    public NPrimitiveElement ofInstant(String value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, DefaultNLiteral.parseInstant(value).get(), null, null);
    }

    @Override
    public NPrimitiveElement ofByte(Byte value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BYTE, value);
    }

    @Override
    public NPrimitiveElement ofByte(Byte value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BYTE, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofByte(byte value) {
        return new DefaultNNumberElement(NElementType.BYTE, value);
    }

    @Override
    public NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BYTE, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.BYTE, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofByte(Byte value, NNumberLayout layout) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BYTE, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofByte(byte value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.BYTE, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofByte(byte value, String suffix) {
        return new DefaultNNumberElement(NElementType.BYTE, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofShort(Short value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value);
    }


    @Override
    public NPrimitiveElement ofShort(short value) {
        return new DefaultNNumberElement(NElementType.SHORT, value, null, null);
    }

    @Override
    public NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.SHORT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofShort(Short value, NNumberLayout layout) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofShort(short value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.SHORT, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofShort(Short value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofShort(short value, String suffix) {
        return new DefaultNNumberElement(NElementType.SHORT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement ofChar(Character value) {
        return value == null ? ofNull() : new DefaultNStringElement(NElementType.CHAR, value, null, null);
    }

    @Override
    public NPrimitiveElement ofDouble(Double value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.DOUBLE, value);
    }

    @Override
    public NPrimitiveElement ofDouble(double value) {
        return new DefaultNNumberElement(NElementType.DOUBLE, value);
    }

    @Override
    public NPrimitiveElement ofDouble(Double value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.DOUBLE, value, NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement ofDouble(double value, String suffix) {
        return new DefaultNNumberElement(NElementType.DOUBLE, value, NNumberLayout.DECIMAL, suffix);
    }


    @Override
    public NPrimitiveElement ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value);
    }

    @Override
    public NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BIG_DECIMAL, value, NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement ofBigInt(BigInteger value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_INT, value);
    }

    @Override
    public NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, layout, null);
    }

    @Override
    public NPrimitiveElement ofBigInt(BigInteger value, String suffix) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, null, suffix);
    }

    @Override
    public NUpletElementBuilder ofUpletBuilder() {
        return new DefaultNUpletElementBuilder();
    }

    @Override
    public NUpletElementBuilder ofUpletBuilder(String name) {
        return ofUpletBuilder().name(name);
    }

    @Override
    public NUpletElement ofUplet() {
        return ofUpletBuilder().build();
    }

    @Override
    public NUpletElement ofUplet(String name, NElement... items) {
        return ofUpletBuilder().name(name).addAll(items).build();
    }

    @Override
    public NUpletElement ofNamedUplet(String name, NElement... items) {
        return ofUpletBuilder().name(name).addAll(items).build();
    }

    @Override
    public NUpletElement ofUplet(NElement... items) {
        return ofUpletBuilder().addAll(items).build();
    }

    @Override
    public NPrimitiveElement ofDoubleComplex(double real) {
        return ofDoubleComplex(real, 0);
    }

    @Override
    public NPrimitiveElement ofDoubleComplex(double real, double imag) {
        return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, new NDoubleComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofFloatComplex(float real) {
        return ofFloatComplex(real, 0);
    }

    @Override
    public NPrimitiveElement ofFloatComplex(float real, float imag) {
        return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, new NFloatComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofBigComplex(BigDecimal real) {
        return ofBigComplex(real, BigDecimal.ZERO);
    }

    @Override
    public NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        if (real == null && imag == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_COMPLEX, new NBigComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofNumber(Number value) {
        if (value == null) {
            return ofNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNNumberElement(NElementType.BYTE, value);
            case "java.lang.Short":
                return new DefaultNNumberElement(NElementType.SHORT, value);
            case "java.lang.Integer":
                return new DefaultNNumberElement(NElementType.INT, value);
            case "java.lang.Long":
                return new DefaultNNumberElement(NElementType.LONG, value);
            case "java.math.BigInteger":
                return new DefaultNNumberElement(NElementType.BIG_INT, value);
            case "java.lang.float":
                return new DefaultNNumberElement(NElementType.FLOAT, value);
            case "java.lang.Double":
                return new DefaultNNumberElement(NElementType.DOUBLE, value);
            case "java.math.BigDecimal":
                return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value);
        }
        if (value instanceof NDoubleComplex) {
            return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, value);
        }
        if (value instanceof NBigComplex) {
            return new DefaultNNumberElement(NElementType.BIG_COMPLEX, value);
        }
        if (value instanceof NFloatComplex) {
            return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, value);
        }
        // ???
        return new DefaultNNumberElement(NElementType.FLOAT, value);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NMatrixElementBuilder ofMatrixBuilder() {
        throw new NUnsupportedOperationException(NMsg.ofC("not implemented yet ofMatrixBuilder()"));
    }

    public NElementComments ofMultiLineComments(String... lines) {
        return new NElementCommentsImpl(new NElementComment[]{ofMultiLineComment(lines)}, null);
    }

    public NElementComments ofSingleLineComments(String... lines) {
        return new NElementCommentsImpl(
                Arrays.stream(lines == null ? new String[0] : lines)
                        .map(x -> ofSingleLineComment(x)).toArray(NElementComment[]::new)
                , null);
    }

    public NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing) {
        return new NElementCommentsImpl(leading, trailing);
    }


    public NElementComment ofMultiLineComment(String... lines) {
        return NElementCommentImpl.ofMultiLine(lines);
    }

    public NElementComment ofSingleLineComment(String... lines) {
        return NElementCommentImpl.ofSingleLine(lines);
    }

    @Override
    public NPrimitiveElementBuilder ofPrimitiveBuilder() {
        return new DefaultNPrimitiveElementBuilder();
    }
}
