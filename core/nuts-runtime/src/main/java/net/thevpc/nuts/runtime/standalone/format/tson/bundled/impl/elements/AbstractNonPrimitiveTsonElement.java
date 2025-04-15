package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Pattern;

public abstract class AbstractNonPrimitiveTsonElement extends AbstractTsonElement {
    public AbstractNonPrimitiveTsonElement(TsonElementType type) {
        super(type);
    }

    protected <T> T throwPrimitive(TsonElementType type) {
        throw new ClassCastException(type() + " is not a primitive. Cannot cast to " + type);
    }

    protected <T> T throwNonPrimitive(TsonElementType type) {
        throw new ClassCastException(type() + " cannot be cast to " + type);
    }

    @Override
    public TsonString toStr() {
        return throwPrimitive(TsonElementType.DOUBLE_QUOTED_STRING);
    }


    @Override
    public TsonLong toLong() {
        return throwPrimitive(TsonElementType.LONG);
    }

    @Override
    public TsonInt toInt() {
        return throwPrimitive(TsonElementType.INTEGER);
    }

    @Override
    public TsonFloat toFloat() {
        return throwPrimitive(TsonElementType.FLOAT);
    }

    @Override
    public TsonDouble toDouble() {
        return throwPrimitive(TsonElementType.DOUBLE);
    }

    @Override
    public TsonShort toShort() {
        return throwPrimitive(TsonElementType.SHORT);
    }

    @Override
    public TsonByte toByte() {
        return throwPrimitive(TsonElementType.BYTE);
    }

    @Override
    public TsonChar toChar() {
        return throwPrimitive(TsonElementType.CHAR);
    }

    @Override
    public TsonBoolean toBoolean() {
        return throwPrimitive(TsonElementType.BOOLEAN);
    }

    @Override
    public TsonCustom toCustom() {
        return throwPrimitive(TsonElementType.CUSTOM);
    }

    @Override
    public TsonName toName() {
        return throwPrimitive(TsonElementType.NAME);
    }

    @Override
    public TsonAlias toAlias() {
        return throwPrimitive(TsonElementType.ALIAS);
    }

    @Override
    public TsonLocalDate toLocalDate() {
        return throwPrimitive(TsonElementType.LOCAL_DATE);
    }

    @Override
    public TsonLocalDateTime toLocalDateTime() {
        return throwPrimitive(TsonElementType.LOCAL_DATETIME);
    }

    @Override
    public TsonLocalTime toLocalTime() {
        return throwPrimitive(TsonElementType.LOCAL_TIME);
    }

    @Override
    public TsonRegex toRegex() {
        return throwPrimitive(TsonElementType.REGEX);
    }

    @Override
    public Boolean booleanObject() {
        return throwPrimitive(TsonElementType.BOOLEAN);
    }

    @Override
    public Character charObject() {
        return throwPrimitive(TsonElementType.CHAR);
    }

    @Override
    public Byte byteObject() {
        return throwPrimitive(TsonElementType.BYTE);
    }

    @Override
    public Long longObject() {
        return throwPrimitive(TsonElementType.LONG);
    }

    @Override
    public Integer intObject() {
        return throwPrimitive(TsonElementType.INTEGER);
    }

    @Override
    public Short shortObject() {
        return throwPrimitive(TsonElementType.SHORT);
    }

    @Override
    public Float floatObject() {
        return throwPrimitive(TsonElementType.FLOAT);
    }

    @Override
    public Double doubleObject() {
        return throwPrimitive(TsonElementType.DOUBLE);
    }

    @Override
    public float floatValue() {
        return throwPrimitive(TsonElementType.FLOAT);
    }

    @Override
    public double doubleValue() {
        return throwPrimitive(TsonElementType.DOUBLE);
    }

    @Override
    public byte byteValue() {
        return throwPrimitive(TsonElementType.BYTE);
    }

    @Override
    public char charValue() {
        return throwPrimitive(TsonElementType.CHAR);
    }

    @Override
    public boolean booleanValue() {
        return throwPrimitive(TsonElementType.BOOLEAN);
    }

    @Override
    public int intValue() {
        return throwPrimitive(TsonElementType.INTEGER);
    }

    @Override
    public long longValue() {
        return throwPrimitive(TsonElementType.LONG);
    }

    @Override
    public short shortValue() {
        return throwPrimitive(TsonElementType.SHORT);
    }

    @Override
    public LocalDate localDateValue() {
        return throwPrimitive(TsonElementType.LOCAL_DATE);
    }

    @Override
    public Instant instantValue() {
        return throwPrimitive(TsonElementType.INSTANT);
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return throwPrimitive(TsonElementType.LOCAL_DATETIME);
    }

    @Override
    public LocalTime localTimeValue() {
        return throwPrimitive(TsonElementType.LOCAL_TIME);
    }

    @Override
    public Pattern regexValue() {
        return throwPrimitive(TsonElementType.REGEX);
    }

    @Override
    public String stringValue() {
        return throwPrimitive(TsonElementType.DOUBLE_QUOTED_STRING);
    }

    @Override
    public TsonArray toArray() {
        return throwNonPrimitive(TsonElementType.ARRAY);
    }

    @Override
    public TsonMatrix toMatrix() {
        return throwNonPrimitive(TsonElementType.MATRIX);
    }

    @Override
    public TsonObject toObject() {
        return throwNonPrimitive(TsonElementType.OBJECT);
    }

    @Override
    public TsonUplet toUplet() {
        return throwNonPrimitive(TsonElementType.UPLET);
    }

    @Override
    public TsonPair toPair() {
        return throwNonPrimitive(TsonElementType.PAIR);
    }

    @Override
    public TsonOp toOp() {
        return throwNonPrimitive(TsonElementType.OP);
    }


    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        return true;
    }

    @Override
    public String toString() {
        return Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(boolean compact) {
        return compact ? Tson.COMPACT_FORMAT.format(this) : Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(TsonFormat format) {
        return format == null ? Tson.DEFAULT_FORMAT.format(this) : format.format(this);
    }
}
