package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;

public class TsonDoubleImpl extends AbstractNumberTsonElement implements TsonDouble {
    private double value;

    public TsonDoubleImpl(double value, String unit) {
        super(TsonElementType.DOUBLE, TsonNumberLayout.DECIMAL, unit);
        this.value = value;
    }

    @Override
    public Number numberValue() {
        return value();
    }

    @Override
    public TsonDouble toDouble() {
        return this;
    }

    @Override
    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonDoubleImpl that = (TsonDoubleImpl) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public TsonByte toByte() {
        return Tson.of((byte) value()).toByte();
    }

    @Override
    public TsonShort toShort() {
        return Tson.of((short) value()).toShort();
    }

    @Override
    public TsonInt toInt() {
        return Tson.of((int) value()).toInt();
    }

    @Override
    public TsonLong toLong() {
        return Tson.of((long) value()).toLong();
    }

    @Override
    public TsonFloat toFloat() {
        return Tson.of((float) value()).toFloat();
    }

    @Override
    public Byte byteObject() {
        return (byte) value();
    }

    @Override
    public Long longObject() {
        return (long) value();
    }

    @Override
    public Integer intObject() {
        return (int) value();
    }

    @Override
    public Short shortObject() {
        return (short) value();
    }

    @Override
    public Float floatObject() {
        return (float) value();
    }

    @Override
    public Double doubleObject() {
        return (double) value();
    }

    //
    @Override
    public byte byteValue() {
        return ((byte) value());
    }

    @Override
    public short shortValue() {
        return ((short) value());
    }

    @Override
    public int intValue() {
        return ((int) value());
    }

    @Override
    public long longValue() {
        return ((long) value());
    }

    @Override
    public float floatValue() {
        return ((float) value());
    }

    @Override
    public double doubleValue() {
        return (double) value();
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
        return Double.compare(value, o.toDouble().value());
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isNumber()) {
            switch (o.type()) {
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG:
                case FLOAT:
                case DOUBLE: {
                    int i = Double.compare(value(), o.doubleValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
            }
        }
        return super.compareTo(o);
    }
}
