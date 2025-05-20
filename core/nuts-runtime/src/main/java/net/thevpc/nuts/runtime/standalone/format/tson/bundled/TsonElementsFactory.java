package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.regex.Pattern;

public interface TsonElementsFactory {

    TsonElement ofString(TsonElementType stringType, String value);

    TsonElement ofNull();

    TsonElement ofBoolean(boolean val);

    TsonElement ofDoubleQuotedString(String value);

    TsonElement ofSingleQuotedString(String value);

    TsonElement ofAntiQuotedString(String value);

    TsonElement ofTripleDoubleQuotedString(String value);

    TsonElement ofTripleSingleQuotedString(String value);

    TsonElement ofTripleAntiQuotedString(String value);

    TsonElement ofLineString(String value);

    TsonElement ofElement(TsonElementBase value);

    TsonElementBase ofElementBase(TsonElementBase value);

    TsonElement ofLocalDatetime(Instant value);

    TsonElement ofLocalDatetime(LocalDateTime value);

    TsonElement ofLocalDatetime(Date value);

    TsonElement ofLocalDate(LocalDate value);

    TsonElement ofLocalTime(LocalTime value);

    TsonElement ofLocalTime(java.sql.Time value);

    TsonElement ofLocalDate(java.sql.Date value);

    TsonElement ofRegex(Pattern value);

    TsonElement ofRegex(String value);

    TsonElement ofChar(char value);

    TsonElement ofInstant(Instant value);

    TsonElement ofInt(int value);

    TsonElement ofInt(int value, TsonNumberLayout layout);

    TsonElement ofInt(int value, TsonNumberLayout layout, String unit);

    TsonElement ofNumber(Number value, TsonNumberLayout layout, String unit);

    TsonElement ofLong(long value);

    TsonElement ofLong(long value, TsonNumberLayout layout);

    TsonElement ofLong(long value, TsonNumberLayout layout, String unit);

    TsonElement ofByte(byte value, TsonNumberLayout layout);

    TsonElement ofByte(byte value, TsonNumberLayout layout, String unit);

    TsonElement ofByte(byte value);

    TsonElement ofShort(short value, TsonNumberLayout layout);

    TsonElement ofShort(short value, TsonNumberLayout layout, String unit);

    TsonElement ofShort(short value);

    TsonElement ofFloat(float value);

    TsonElement ofFloat(float value, String unit);

    TsonElement ofBigInt(BigInteger value);

    TsonElement ofBigInt(BigInteger value, TsonNumberLayout layout);

    TsonElement ofBigInt(BigInteger value, TsonNumberLayout layout, String unit);

    TsonElement ofBigDecimal(BigDecimal value);

    TsonElement ofBigDecimal(BigDecimal value, String unit);

    TsonElement ofBigComplex(BigDecimal real, BigDecimal imag);

    TsonElement ofBigComplex(BigDecimal real, BigDecimal imag, String unit);

    TsonElement ofFloatComplex(float real, float imag);

    TsonElement ofFloatComplex(float real, float imag, String unit);

    TsonElement ofDoubleComplex(double real, double imag);

    TsonElement ofDoubleComplex(double real, double imag, String unit);

    TsonElement ofDouble(double value);

    TsonElement ofDouble(double value, String unit);

    TsonElement ofName(String value);

    TsonElement ofAlias(String value);

    TsonOpBuilder ofOpBuilder();

    TsonElement of(boolean value);

    TsonElement of(char value);

    TsonElement of(byte value);

    TsonElement of(short value);

    TsonElement of(int value);

    TsonElement of(long value);

    TsonElement of(float value);

    TsonElement of(double value);

    TsonElement of(BigInteger value);

    TsonElement of(BigDecimal value);

    TsonElement of(byte[] value);

    TsonElement of(InputStream value);

    TsonElement ofBinStream(byte[] value);

    TsonElement ofBinStream(InputStream value);

    TsonElement ofBinStream(File value);

    TsonElement ofBinStream(Path value);

    TsonElement ofCharStream(char[] value, String type);

    TsonElement ofCharStream(Reader value, String type);

    TsonElement ofCharStream(File value, String type);

    TsonElement ofCharStream(String value);

    TsonElement ofCharStream(char[] value);

    TsonElement ofCharStream(Reader value);

    TsonElement ofCharStream(File value);

    TsonElement ofCharStream(Path value);

    TsonElement ofCharStream(String value, String language);

    TsonElement ofCharStream(Path value, String language);

    TsonElement ofStopStream(String value, String stopWord);

    TsonElement ofStopStream(char[] value, String stopWord);

    TsonElement ofStopStream(Reader value, String stopWord);

    TsonElement ofStopStream(File value, String stopWord);

    TsonElement ofStopStream(Path value, String stopWord);

    TsonElement of(Boolean value);

    TsonElement of(Character value);

    TsonElement of(Byte value);

    TsonElement of(Short value);

    TsonElement of(Integer value);

    TsonElement of(Long value);

    TsonElement of(Float value);

    TsonElement of(Double value);

    TsonElement of(Date value);

    TsonElement of(Instant value);

    TsonElement of(LocalDate value);

    TsonElement of(java.sql.Date value);

    TsonElement of(java.sql.Time value);

    TsonElement of(LocalTime value);

    TsonElement of(Pattern value);

    TsonPrimitiveBuilder of();

    TsonArrayBuilder ofArrayBuilder();

    TsonMatrixBuilder ofMatrixBuilder();

    TsonPairBuilder ofPairBuilder();

    TsonObjectBuilder ofObjBuilder();

    TsonUpletBuilder ofUpletBuilder();

    TsonAnnotationBuilder ofAnnotationBuilder();

    TsonFormatBuilder format();

    TsonDocumentBuilder ofDocument();

    TsonDocumentHeaderBuilder ofDocumentHeader();

    TsonElement of(Enum b);

    TsonElement of(TsonElementBase b);

    TsonProcessor processor();

    TsonBinaryStreamBuilder ofBinStreamBuilder();


    TsonElement parseLocalDateTime(String s);

    TsonElement parseInstant(String s);

    TsonElement parseLocalDate(String s);

    TsonElement parseLocalTime(String s);

    TsonElement parseRegex(String s);

    TsonElement parseNumber(String s);

    TsonElement parseChar(String s);

    TsonElement parseString(String s);

    TsonElement parseAlias(String s);

    TsonComment parseComments(String c);

    CharStreamCodeSupport charStreamCodeSupportOf(String language);

    TsonElement ofCustom(Object o);

    TsonPrimitiveBuilder ofPrimitiveBuilder();
}
