package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;

public class TsonByteImpl extends AbstractNumberTsonElement implements TsonByte {
    private byte value;

    public TsonByteImpl(byte value,TsonNumberLayout layout,String unit) {
        super(TsonElementType.BYTE,layout,unit);
        this.value = value;
    }

    @Override
    public Number numberValue() {
        return value();
    }

    @Override
    public TsonByte toByte() {
        return this;
    }

    @Override
    public byte value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonByteImpl tsonByte = (TsonByteImpl) o;
        return value == tsonByte.value;
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
        return Byte.compare(value, o.toByte().value());
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
    public int compareTo(TsonElement o) {
        if (o.type().isNumber()) {
            switch (o.type()) {
                case BYTE: {
                    int i= Byte.compare(value(), o.byteValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
                case SHORT: {
                    int i= Short.compare(value(), o.shortValue());
                    return i == 0 ? type().compareTo(o.type()):i;
                }
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
