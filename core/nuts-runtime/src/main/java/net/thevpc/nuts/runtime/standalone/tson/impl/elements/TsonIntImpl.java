package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;

public class TsonIntImpl extends AbstractNumberTsonElement implements TsonInt {
    private int value;

    public TsonIntImpl(int value,TsonNumberLayout layout,String unit) {
        super(TsonElementType.INTEGER,layout,unit);
        this.value = value;
    }

    @Override
    public Number numberValue() {
        return value();
    }

    @Override
    public TsonInt toInt() {
        return this;
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonIntImpl tsonInt = (TsonIntImpl) o;
        return value == tsonInt.value;
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
    public TsonLong toLong() {
        return Tson.of((long) value()).toLong();
    }

    @Override
    public TsonFloat toFloat() {
        return Tson.of((float) value()).toFloat();
    }

    @Override
    public TsonDouble toDouble() {
        return Tson.of((double) value()).toDouble();
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
        return Integer.compare(value, o.toInt().value());
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isNumber()) {
            switch (o.type()) {
                case BYTE:
                case SHORT:
                case INTEGER: {
                    int i= Integer.compare(value(), o.intValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
                case LONG: {
                    int i= Long.compare(value(), o.longValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
                case FLOAT: {
                    int i= Float.compare(value(), o.floatValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
                case DOUBLE: {
                    int i= Double.compare(value(), o.doubleValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
            }
        }
        return super.compareTo(o);
    }
}
