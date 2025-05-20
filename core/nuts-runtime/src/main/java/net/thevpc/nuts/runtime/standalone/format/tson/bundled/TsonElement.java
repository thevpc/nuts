package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.regex.Pattern;

public interface TsonElement extends TsonElementBase, Comparable<TsonElement> {
    int annotationsCount();

    List<TsonAnnotation> annotations();

    String toString(boolean compact);

    TsonComments comments();

    TsonString toStr();

    TsonNumber toNumber();

    TsonLong toLong();

    TsonInt toInt();

    TsonFloat toFloat();

    TsonDouble toDouble();

    TsonShort toShort();

    TsonByte toByte();

    TsonChar toChar();

    TsonBoolean toBoolean();

    TsonCustom toCustom();

    TsonName toName();

    TsonAlias toAlias();

    TsonLocalDate toLocalDate();

    TsonLocalDateTime toLocalDateTime();
    TsonInstant toInstant();

    TsonLocalTime toLocalTime();

    TsonRegex toRegex();

    TsonMatrix toMatrix();

    TsonArray toArray();

    TsonListContainer toListContainer();

    TsonBinaryStream toBinaryStream();

    TsonCharStream toCharStream();

    TsonObject toObject();

    TsonUplet toUplet();

    TsonPair toPair();

    TsonOp toOp();

    TsonBigInt toBigInt();

    TsonBigDecimal toBigDecimal();

    TsonBigComplex toBigComplex();

    TsonFloatComplex toFloatComplex();

    TsonDoubleComplex toDoubleComplex();

    String stringValue();

    boolean booleanValue();

    char charValue();

    byte byteValue();

    int intValue();

    long longValue();

    short shortValue();

    float floatValue();

    double doubleValue();

    BigInteger bigIntegerValue();

    BigDecimal bigDecimalValue();

    Boolean booleanObject();

    Character charObject();

    Number numberValue();

    Temporal temporalValue();

    Byte byteObject();

    Integer intObject();

    Long longObject();

    Short shortObject();

    Float floatObject();

    Double doubleObject();

    LocalDateTime localDateTimeValue();

    LocalDate localDateValue();

    Instant instantValue();

    LocalTime localTimeValue();

    Pattern regexValue();

    TsonElementBuilder builder();

    boolean visit(TsonDocumentVisitor visitor);

    void visit(TsonParserVisitor visitor);

    @Override
    default TsonElement build() {
        return this;
    }

    boolean isNull();

    boolean isListContainer();

    boolean isNumber();

    boolean isOrdinalNumber();

    boolean isFloatingNumber();

    boolean isComplexNumber();

    boolean isBoolean();

    boolean isName();

    boolean isArray();

    boolean isNamedArray();

    boolean isObject();

    boolean isNamedObject();

    boolean isNamedUplet();


    boolean isUplet();

    boolean isPair();

    boolean isSimple();

    boolean isSimplePair();

    boolean isString();

    boolean isAnyString();

    boolean isPrimitive();

    boolean isTemporal();
}
