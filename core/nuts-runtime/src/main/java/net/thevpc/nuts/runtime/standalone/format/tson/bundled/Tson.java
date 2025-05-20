package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.TsonElementsFactoryImpl;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

public class Tson {

    private static String VERSION = "1.0";
    private static TsonElementsFactory factory = new TsonElementsFactoryImpl();


    public static final TsonFormat COMPACT_FORMAT = format().compact(true).build();
    public static final TsonFormat DEFAULT_FORMAT = format().build();


    public static String getVersion() {
        return VERSION;
    }

    public static TsonElement ofTrue() {
        return of(true);
    }

    public static TsonElement ofFalse() {
        return of(false);
    }

    public static TsonElement ofNull() {
        return factory.ofNull();
    }

    public static TsonElement ofBoolean(boolean val) {
        return factory.ofBoolean(val);
    }

    public static TsonElement ofString(TsonElementType stringType, String value) {
        return factory.ofString(stringType, value);
    }

    public static TsonElement ofString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofDoubleQuotedString(value);
    }

    public static TsonElement ofDoubleQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofDoubleQuotedString(value);
    }

    public static TsonElement ofSingleQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofSingleQuotedString(value);
    }

    public static TsonElement ofAntiQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofAntiQuotedString(value);
    }

    public static TsonElement ofTripleDoubleQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofTripleDoubleQuotedString(value);
    }

    public static TsonElement ofTripleSingleQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofTripleSingleQuotedString(value);
    }

    public static TsonElement ofTripleAntiQuotedString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofTripleAntiQuotedString(value);
    }

    public static TsonElement ofLineString(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLineString(value);
    }

    public static TsonElement ofElement(TsonElementBase value) {
        if (value == null) {
            return ofNull();
        }
        return value.build();
    }

    public static TsonElementBase ofElementBase(TsonElementBase value) {
        if (value == null) {
            return ofNull();
        }
        return value;
    }


    public static TsonElement ofInstant(Instant value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofInstant(value);
    }

    public static TsonElement ofLocalDatetime(Instant value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalDatetime(value);
    }

    public static TsonElement ofLocalDatetime(Date value) {
        if (value == null) {
            return ofNull();
        }
        return ofLocalDatetime(Instant.ofEpochMilli(value.getTime()));
    }

    public static TsonElement ofLocalDatetime(LocalDateTime value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalDatetime(value);
    }

    public static TsonElement ofLocalDate(LocalDate value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalDate(value);
    }

    public static TsonElement ofLocalTime(LocalTime value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalTime(value);
    }

    public static TsonElement ofLocalTime(java.sql.Time value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalTime(value.toLocalTime());
    }

    public static TsonElement ofLocalDate(java.sql.Date value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofLocalDate(value.toLocalDate());
    }

    public static TsonElement ofRegex(Pattern value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofRegex(value);
    }

    public static TsonElement ofRegex(String value) {
        if (value == null) {
            return ofNull();
        }
        return ofRegex(Pattern.compile(value));
    }

    public static TsonPrimitiveBuilder ofPrimitiveBuilder() {
        return factory.ofPrimitiveBuilder();
    }

    public static TsonElement ofChar(char value) {
        return factory.ofChar(value);
    }

    public static TsonElement ofNumber(Number value, TsonNumberLayout layout, String unit) {
        return factory.ofNumber(value, layout, unit);
    }

    public static TsonElement ofInt(int value) {
        return ofInt(value, TsonNumberLayout.DECIMAL);
    }

    public static TsonElement ofInt(int value, TsonNumberLayout layout) {
        return factory.ofInt(value, layout, null);
    }

    public static TsonElement ofInt(int value, TsonNumberLayout layout, String unit) {
        return factory.ofInt(value, layout, unit);
    }

    public static TsonElement ofLong(long value) {
        return ofLong(value, TsonNumberLayout.DECIMAL);
    }

    public static TsonElement ofLong(long value, TsonNumberLayout layout) {
        return factory.ofLong(value, layout, null);
    }

    public static TsonElement ofLong(long value, TsonNumberLayout layout, String unit) {
        return factory.ofLong(value, layout, unit);
    }

    public static TsonElement ofByte(byte value, TsonNumberLayout layout) {
        return factory.ofByte(value, layout, null);
    }

    public static TsonElement ofByte(byte value, TsonNumberLayout layout, String unit) {
        return factory.ofByte(value, layout, unit);
    }

    public static TsonElement ofByte(byte value) {
        return ofByte(value, TsonNumberLayout.DECIMAL);
    }

    public static TsonElement ofShort(short value, TsonNumberLayout layout) {
        return factory.ofShort(value, layout, null);
    }

    public static TsonElement ofShort(short value, TsonNumberLayout layout, String unit) {
        return factory.ofShort(value, layout, unit);
    }

    public static TsonElement ofShort(short value) {
        return ofShort(value, TsonNumberLayout.DECIMAL);
    }

    public static TsonElement ofFloat(float value) {
        return factory.ofFloat(value, null);
    }

    public static TsonElement ofFloat(float value, String unit) {
        return factory.ofFloat(value, unit);
    }

    public static TsonElement ofBigInt(BigInteger value) {
        return ofBigInt(value, TsonNumberLayout.DECIMAL);
    }

    public static TsonElement ofBigInt(BigInteger value, TsonNumberLayout layout) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBigInt(value, layout, null);
    }

    public static TsonElement ofBigInt(BigInteger value, TsonNumberLayout layout, String unit) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBigInt(value, layout, unit);
    }

    public static TsonElement ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBigDecimal(value, null);
    }

    public static TsonElement ofBigDecimal(BigDecimal value, String unit) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBigDecimal(value, unit);
    }

    public static TsonElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        if (real == null && imag == null) {
            return ofNull();
        }
        if (real == null || imag == null) {
            throw new IllegalArgumentException("Null real or imag");
        }
        return factory.ofBigComplex(real, imag, null);
    }

    public static TsonElement ofBigComplex(BigDecimal real, BigDecimal imag, String unit) {
        if (real == null && imag == null) {
            return ofNull();
        }
        if (real == null || imag == null) {
            throw new IllegalArgumentException("Null real or imag");
        }
        return factory.ofBigComplex(real, imag, unit);
    }

    public static TsonElement ofFloatComplex(float real, float imag) {
        return factory.ofFloatComplex(real, imag, null);
    }

    public static TsonElement ofFloatComplex(float real, float imag, String unit) {
        return factory.ofFloatComplex(real, imag, unit);
    }


    public static TsonElement ofDoubleComplex(double real, double imag) {
        return factory.ofDoubleComplex(real, imag, null);
    }

    public static TsonElement ofDoubleComplex(double real, double imag, String unit) {
        return factory.ofDoubleComplex(real, imag, unit);
    }


    public static TsonElement ofDouble(double value) {
        return factory.ofDouble(value, null);
    }

    public static TsonElement ofDouble(double value, String unit) {
        return factory.ofDouble(value, unit);
    }

    public static TsonElement ofName(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofName(value);
    }

    public static TsonElement ofAlias(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofAlias(value);
    }

    public static TsonPair ofPair(TsonElementBase key, TsonElementBase value) {
        return factory.ofPairBuilder().key(
                of(key)
        ).value(of(value)).build();
    }

    public static TsonOp binOp(String op, TsonElementBase key, TsonElementBase value) {
        return factory.ofOpBuilder()
                .opName(op)
                .opType(TsonOpType.BINARY)
                .first(of(key))
                .second(of(value))
                .build();
    }

    public static TsonPair ofPair(String key, TsonElementBase value) {
        return factory.ofPairBuilder().key(ofName(key)).value(of(value)).build();
    }

    public static TsonElement of(boolean value) {
        return ofBoolean(value);
    }

    public static TsonElement of(Number value) {
        if (value == null) {
            return ofNull();
        }
        switch (value.getClass().getName()) {
            case "byte":
            case "java.lang.Byte":
                return of(value.byteValue());
            case "short":
            case "java.lang.Short":
                return of(value.shortValue());
            case "int":
            case "java.lang.Integer":
                return of(value.intValue());
            case "long":
            case "java.lang.Long":
                return of(value.longValue());
            case "float":
            case "java.lang.Float":
                return of(value.floatValue());
            case "double":
            case "java.lang.Double":
                return of(value.doubleValue());
            case "java.math.BigInteger":
                return of((java.math.BigInteger) value);
            case "java.math.BigDecimal":
                return of((java.math.BigDecimal) value);
        }
        throw new IllegalArgumentException("Unsupported number type: " + value.getClass().getName());
    }

    public static TsonElement of(char value) {
        return ofChar(value);
    }

    public static TsonElement of(byte value) {
        return ofByte(value);
    }

    public static TsonElement of(short value) {
        return ofShort(value);
    }

    public static TsonElement of(int value) {
        return ofInt(value);
    }

    public static TsonElement of(long value) {
        return ofLong(value);
    }

    public static TsonElement of(float value) {
        return ofFloat(value);
    }

    public static TsonElement of(double value) {
        return ofDouble(value);
    }

    public static TsonElement of(BigInteger value) {
        return ofBigInt(value);
    }

    public static TsonElement of(BigDecimal value) {
        return ofBigDecimal(value);
    }

    public static TsonElement of(byte[] value) {
        if (value == null) {
            return ofNull();
        }
        return ofBinStream(value);
    }

    public static TsonElement of(InputStream value) {
        if (value == null) {
            return ofNull();
        }
        return ofBinStream(value);
    }

    public static TsonBinaryStreamBuilder ofBinStream() {
        return factory.ofBinStreamBuilder();
    }

    public static TsonElement ofBinStream(byte[] value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBinStream(value);
    }

    public static TsonElement ofBinStream(InputStream value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBinStream(value);
    }

    public static TsonElement ofBinStream(File value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBinStream(value);
    }

    public static TsonElement ofBinStream(Path value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofBinStream(value);
    }

    private static TsonElement ofCharStream(char[] value, String type) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, type);
    }

    private static TsonElement ofCharStream(Reader value, String type) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, type);
    }

    private static TsonElement ofCharStream(File value, String type) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, type);
    }

    public static TsonElement ofCharStream(String value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, "");
    }

    public static TsonElement ofCharStream(char[] value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, "");
    }

    public static TsonElement ofCharStream(Reader value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, "");
    }

    public static TsonElement ofCharStream(File value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, "");
    }

    public static TsonElement ofCharStream(Path value) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, "");
    }

    public static TsonElement ofCharStream(String value, String language) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, language);
    }

    public static TsonElement ofCharStream(Path value, String language) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, language);
    }

    public static TsonElement ofStopStream(String value, String stopWord) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, stopWord);
    }

    public static TsonElement ofStopStream(char[] value, String stopWord) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, stopWord);
    }

    public static TsonElement ofStopStream(Reader value, String stopWord) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, stopWord);
    }

    public static TsonElement ofStopStream(File value, String stopWord) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, stopWord);
    }

    public static TsonElement ofStopStream(Path value, String stopWord) {
        if (value == null) {
            return ofNull();
        }
        return factory.ofStopStream(value, stopWord);
    }

    public static TsonElement of(Boolean value) {
        if (value == null) {
            return ofNull();
        }
        return ofBoolean(value);
    }

    public static TsonElement of(Character value) {
        if (value == null) {
            return ofNull();
        }
        return ofChar(value);
    }

    public static TsonElement of(Byte value) {
        if (value == null) {
            return ofNull();
        }
        return ofByte(value);
    }

    public static TsonElement of(Short value) {
        if (value == null) {
            return ofNull();
        }
        return ofShort(value);
    }

    public static TsonElement of(Integer value) {
        if (value == null) {
            return ofNull();
        }
        return ofInt(value);
    }

    public static TsonElement of(Long value) {
        if (value == null) {
            return ofNull();
        }
        return ofLong(value);
    }

    public static TsonElement of(Float value) {
        if (value == null) {
            return ofNull();
        }
        return ofFloat(value);
    }

    public static TsonElement of(Double value) {
        if (value == null) {
            return ofNull();
        }
        return ofDouble(value);
    }

    public static TsonElement of(Date value) {
        if (value == null) {
            return ofNull();
        }
        if (value instanceof java.sql.Time) {
            return ofLocalTime((Time) value);
        }
        if (value instanceof java.sql.Date) {
            return ofLocalDate(((java.sql.Date) value).toLocalDate());
        }
        return ofLocalDatetime(Instant.ofEpochMilli(value.getTime()));
    }

    public static TsonElement of(Instant value) {
        return ofInstant(value);
    }

    public static TsonElement of(LocalDate value) {
        return ofLocalDate(value);
    }

    public static TsonElement of(LocalDateTime value) {
        return ofLocalDatetime(value);
    }

    public static TsonElement of(java.sql.Date value) {
        return ofLocalDate(value);
    }

    public static TsonElement of(java.sql.Time value) {
        return ofLocalTime(value);
    }

    public static TsonElement of(LocalTime value) {
        return ofLocalTime(value);
    }

    public static TsonElement of(Pattern value) {
        return ofRegex(value);
    }

    public static TsonElement of(String value) {
        return ofString(value);
    }

    public static TsonElement of(String[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(boolean[] value) {
        if (value == null) {
            return ofNull();
        }
        TsonArrayBuilder a = ofArrayBuilder();
        for (boolean b : value) {
            a.add(of(b));
        }
        return a.build();
    }

    public static TsonElement of(short[] value) {
        if (value == null) {
            return ofNull();
        }
        TsonArrayBuilder a = ofArrayBuilder();
        for (short b : value) {
            a.add(of(b));
        }
        return a.build();
    }

    public static TsonElement of(float[] value) {
        if (value == null) {
            return ofNull();
        }
        TsonArrayBuilder a = ofArrayBuilder();
        for (float b : value) {
            a.add(of(b));
        }
        return a.build();
    }

    public static TsonElement of(int[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).mapToObj(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(long[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).mapToObj(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(double[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).mapToObj(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }


    public static TsonElement of(Boolean[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(Short[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(Float[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(Integer[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(Long[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonElement of(Double[] value) {
        return value == null ? ofNull() : ofArray(Arrays.stream(value).map(x -> Tson.of(x)).toArray(TsonElementBase[]::new)).build();
    }

    public static TsonPrimitiveBuilder of() {
        return factory.of();
    }

    public static TsonArray ofArray(TsonElementBase... elements) {
        return ofArrayBuilder().addAll(elements).build();
    }

    public static TsonArrayBuilder ofArrayBuilder() {
        return factory.ofArrayBuilder();
    }

    public static TsonArrayBuilder ofArrayBuilder(String name) {
        return ofArrayBuilder().name(name);
    }

    public static TsonArray ofArray(String name, TsonElementBase[] params, TsonElementBase... elems) {
        return ofArrayBuilder().name(name).addParams(params).addAll(elems).build();
    }

    public static TsonArray ofArray(String name, TsonElementBase... elems) {
        return ofArrayBuilder().name(name).addAll(elems).build();
    }

    public static TsonMatrixBuilder ofMatrixBuilder() {
        return factory.ofMatrixBuilder();
    }

    public static TsonMatrixBuilder ofMatrixBuilder(String name) {
        return ofMatrixBuilder().name(name);
    }


    public static TsonMatrixBuilder ofMatrixBuilder(String name, TsonElementBase[] params) {
        return ofMatrixBuilder().name(name).addParams(params);
    }


    public static TsonElementBuilder ofPair() {
        return factory.ofPairBuilder();
    }

    public static TsonObjectBuilder ofObjectBuilder() {
        return factory.ofObjBuilder();
    }

    public static TsonObjectBuilder ofObjectBuilder(TsonElementBase... elems) {
        return ofObjectBuilder().addAll(elems);
    }

    public static TsonObject ofObject(TsonElementBase... elems) {
        return ofObjectBuilder().addAll(elems).build();
    }

    public static TsonObjectBuilder ofObjectBuilder(String name) {
        TsonObjectBuilder e = ofObjectBuilder();
        e.name(name);
        return e;
    }

    public static TsonObjectBuilder ofObjectBuilder(String name, TsonElementBase[] params, TsonElementBase... elems) {
        TsonObjectBuilder o = ofObjectBuilder();
        o.name(name).addParams(params);
        return o.addAll(elems);
    }

    public static TsonObjectBuilder ofObjectBuilder(String name, TsonElementBase... elems) {
        TsonObjectBuilder o = ofObjectBuilder();
        o.name(name);
        return o.addAll(elems);
    }

    public static TsonUpletBuilder ofUpletBuilder() {
        return factory.ofUpletBuilder();
    }

    public static TsonUplet ofUplet(TsonElementBase... elements) {
        return factory.ofUpletBuilder().addAll(elements).build();
    }

    public static TsonUplet ofUplet(String name, TsonElementBase... elems) {
        return factory.ofUpletBuilder().name(name).addAll(elems).build();
    }

    public static TsonAnnotationBuilder ofAnnotationBuilder() {
        return factory.ofAnnotationBuilder();
    }

    public static TsonAnnotation ofAnnotation(String name, TsonElementBase... elements) {
        return ofAnnotationBuilder().name(name).addAll(elements).build();
    }

    public static TsonFormatBuilder format() {
        return factory.format();
    }

    public static TsonDocumentBuilder ofDocument() {
        return factory.ofDocument();
    }

    public static TsonDocumentHeaderBuilder ofDocumentHeader() {
        return factory.ofDocumentHeader();
    }

    public static TsonProcessor processor() {
        return factory.processor();
    }

    public static TsonElement of(Enum b) {
        return b == null ? ofNull() : ofName(b.name());
    }

    public static TsonElement of(TsonElementBase b) {
        return b == null ? ofNull() : b.build();
    }

    public static TsonElement parseLocalDateTime(String image) {
        return factory.parseLocalDateTime(image);
    }

    public static TsonElement parseInstant(String image) {
        return factory.parseInstant(image);
    }

    public static TsonElement parseNumber(String image) {
        return factory.parseNumber(image);
    }

    public static TsonElement parseChar(String image) {
        return factory.parseChar(image);
    }

    public static TsonElement parseString(String image) {
        return factory.parseString(image);
    }

    public static TsonElement parseAlias(String image) {
        return factory.parseAlias(image);
    }

    public static TsonElement parseLocalDate(String image) {
        return factory.parseLocalDate(image);
    }

    public static TsonElement parseLocalTime(String image) {
        return factory.parseLocalTime(image);
    }

    public static TsonElement parseRegex(String image) {
        return factory.parseRegex(image);
    }

    public static TsonComment parseComments(String image) {
        return factory.parseComments(image);
    }

    public static CharStreamCodeSupport charStreamCodeSupportOf(String language) {
        return factory.charStreamCodeSupportOf(language);
    }

    public static TsonElement ofCustom(Object o) {
        return factory.ofCustom(o);
    }
}
