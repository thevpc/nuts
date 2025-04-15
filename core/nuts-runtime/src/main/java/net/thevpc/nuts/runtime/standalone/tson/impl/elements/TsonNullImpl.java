package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TsonNullImpl extends AbstractTsonElementBase implements TsonNull {
    public static final TsonNull INSTANCE = new TsonNullImpl();

    public TsonNullImpl() {
    }

    @Override
    public Temporal temporalValue() {
        return null;
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    public TsonElementType type() {
        return TsonElementType.NULL;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Boolean booleanObject() {
        return null;
    }

    @Override
    public Character charObject() {
        return null;
    }

    @Override
    public Byte byteObject() {
        return null;
    }

    @Override
    public Integer intObject() {
        return null;
    }

    @Override
    public Instant instantValue() {
        return null;
    }

    @Override
    public Long longObject() {
        return null;
    }

    @Override
    public Short shortObject() {
        return null;
    }

    @Override
    public Float floatObject() {
        return null;
    }

    @Override
    public Number numberValue() {
        return null;
    }

    @Override
    public Double doubleObject() {
        return null;
    }

    @Override
    public short shortValue() {
        throw new NullPointerException();
    }

    @Override
    public LocalDate localDateValue() {
        return null;
    }

    @Override
    public TsonLocalDateTime toLocalDateTime() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return null;
    }

    @Override
    public LocalTime localTimeValue() {
        return null;
    }

    @Override
    public Pattern regexValue() {
        return null;
    }

    @Override
    public float floatValue() {
        throw new NullPointerException();
    }

    @Override
    public double doubleValue() {
        throw new NullPointerException();
    }

    @Override
    public byte byteValue() {
        throw new NullPointerException();
    }

    @Override
    public char charValue() {
        throw new NullPointerException();
    }

    @Override
    public boolean booleanValue() {
        throw new NullPointerException();
    }

    @Override
    public int intValue() {
        throw new NullPointerException();
    }

    @Override
    public long longValue() {
        throw new NullPointerException();
    }

    @Override
    public TsonComments comments() {
        return null;
    }

    @Override
    public List<TsonAnnotation> annotations() {
        return Collections.emptyList();
    }

    @Override
    public int annotationsCount() {
        return 0;
    }
    //element implementation

    @Override
    public TsonNumber toNumber() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonPair toPair() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonString toStr() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonLong toLong() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonInt toInt() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonFloat toFloat() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonDouble toDouble() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonShort toShort() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonByte toByte() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonChar toChar() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonBoolean toBoolean() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonCustom toCustom() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonName toName() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonAlias toAlias() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonLocalDate toLocalDate() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonLocalTime toLocalTime() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonRegex toRegex() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonArray toArray() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonObject toObject() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonUplet toUplet() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonCharStream toCharStream() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonBigInt toBigInt() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonBigDecimal toBigDecimal() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonBigComplex toBigComplex() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonFloatComplex toFloatComplex() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonDoubleComplex toDoubleComplex() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonBinaryStream toBinaryStream() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonMatrix toMatrix() {
        throw new ClassCastException("Cannot cast Null to Non Null type");
    }

    @Override
    public TsonOp toOp() {
        throw new ClassCastException("Cannot cast Null to Bin Op type");
    }

    @Override
    public TsonListContainer toListContainer() {
        throw new ClassCastException("Cannot cast Null to Container type");
    }

    @Override
    public BigInteger bigIntegerValue() {
        return null;
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return null;
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

    @Override
    public int hashCode() {
        return 96;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TsonNull;
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type() == TsonElementType.NULL) {
            return 0;
        }
        return -1;
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();
        visitor.visitPrimitiveEnd(this);
    }

    @Override
    public boolean isNull() {
        return true;
    }
}
