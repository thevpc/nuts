package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TsonBigDecimalImpl extends AbstractNumberTsonElement implements TsonBigDecimal {
    private BigDecimal value;

    public TsonBigDecimalImpl(BigDecimal value,String unit) {
        super(TsonElementType.BIG_DECIMAL,TsonNumberLayout.DECIMAL,unit);
        this.value = value;
    }

    @Override
    public Number numberValue() {
        return value();
    }

    @Override
    public TsonBigDecimal toBigDecimal() {
        return this;
    }

    @Override
    public BigDecimal value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonBigDecimalImpl tsonInt = (TsonBigDecimalImpl) o;
        return value.equals(tsonInt.value);
    }

    @Override
    public TsonByte toByte() {
        return Tson.of(this.numberValue().byteValue()).toByte();
    }

    @Override
    public TsonShort toShort() {
        return Tson.of(this.numberValue().shortValue()).toShort();
    }

    @Override
    public TsonLong toLong() {
        return Tson.of(this.numberValue().longValue()).toLong();
    }

    @Override
    public TsonFloat toFloat() {
        return Tson.of(this.numberValue().floatValue()).toFloat();
    }

    @Override
    public TsonDouble toDouble() {
        return Tson.of(this.numberValue().doubleValue()).toDouble();
    }

    @Override
    public Byte byteObject() {
        return this.numberValue().byteValue();
    }

    @Override
    public Long longObject() {
        return this.numberValue().longValue();
    }

    @Override
    public Integer intObject() {
        return this.numberValue().intValue();
    }

    @Override
    public Short shortObject() {
        return this.numberValue().shortValue();
    }

    @Override
    public Float floatObject() {
        return this.numberValue().floatValue();
    }

    @Override
    public Double doubleObject() {
        return this.numberValue().doubleValue();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return value();
    }

    @Override
    public BigInteger bigIntegerValue() {
        return value().toBigInteger();
    }


    public TsonBigInt toBigInt(){
        return new TsonBigIntImpl(this.bigIntegerValue(), numberLayout(), numberSuffix());
    }

    @Override
    public byte byteValue() {
        return this.numberValue().byteValue();
    }

    @Override
    public short shortValue() {
        return this.numberValue().shortValue();
    }

    @Override
    public int intValue() {
        return this.numberValue().intValue();
    }

    @Override
    public long longValue() {
        return this.numberValue().longValue();
    }

    @Override
    public float floatValue() {
        return this.numberValue().floatValue();
    }

    @Override
    public double doubleValue() {
        return this.numberValue().doubleValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return value.compareTo(o.toBigDecimal().value());
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isNumber()) {
            switch (o.type()) {
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG:
                case BIG_INTEGER:
                case FLOAT:
                case DOUBLE:
                case BIG_DECIMAL:
                    {
                    int i= this.bigDecimalValue().compareTo(o.bigDecimalValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
            }
        }
        return super.compareTo(o);
    }
}
