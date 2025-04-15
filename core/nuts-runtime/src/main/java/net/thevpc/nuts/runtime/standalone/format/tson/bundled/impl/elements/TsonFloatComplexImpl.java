package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TsonFloatComplexImpl extends AbstractNumberTsonElement implements TsonFloatComplex {
    private float real;
    private float imag;

    public TsonFloatComplexImpl(float real, float imag, String unit) {
        super(TsonElementType.FLOAT_COMPLEX, TsonNumberLayout.DECIMAL, unit);
        this.real = real;
        this.imag = imag;
    }

    @Override
    public Number numberValue() {
        return new TsonComplex(real, imag);
    }

    @Override
    public TsonFloatComplex toFloatComplex() {
        return this;
    }

    @Override
    public TsonDoubleComplex toDoubleComplex() {
        return new TsonDoubleComplexImpl(real(), imag(), numberSuffix());
    }

    @Override
    public TsonBigDecimal toBigDecimal() {
        return new TsonBigDecimalImpl(BigDecimal.valueOf(real()), numberSuffix());
    }

    @Override
    public float real() {
        return real;
    }

    @Override
    public float imag() {
        return imag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonFloatComplexImpl tsonInt = (TsonFloatComplexImpl) o;
        return real == (tsonInt.real) && imag == (tsonInt.imag);
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
        return BigDecimal.valueOf(real());
    }

    @Override
    public BigInteger bigIntegerValue() {
        return this.bigDecimalValue().toBigInteger();
    }


    public TsonBigInt toBigInt() {
        return new TsonBigIntImpl(bigIntegerValue(), numberLayout(), numberSuffix());
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
        return Objects.hash(super.hashCode(), real, imag);
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return compare(this, o.toFloatComplex());
    }

    public static int compare(TsonFloatComplex a, TsonFloatComplex oc) {
        int c = Float.compare(a.real(), oc.real());
        if (c != 0) {
            return c;
        }
        c = Float.compare(a.imag(), oc.imag());
        if (c != 0) {
            return c;
        }
        return c;
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
                case DOUBLE: {
                    int i = this.toDoubleComplex().compareTo(o.toDoubleComplex());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
                case BIG_DECIMAL:
                case BIG_COMPLEX: {
                    int i = this.toBigComplex().compareTo(o.toBigComplex());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
            }
        }
        return super.compareTo(o);
    }

    private static class TsonComplex extends Number {
        private float real;
        private float image;

        public TsonComplex(float real, float image) {
            this.real = real;
            this.image = image;
        }

        public double getReal() {
            return real;
        }

        public double getImage() {
            return image;
        }

        @Override
        public int intValue() {
            return (int) real;
        }

        @Override
        public long longValue() {
            return (long) real;
        }

        @Override
        public float floatValue() {
            return (float) real;
        }

        @Override
        public double doubleValue() {
            return (double) real;
        }

        @Override
        public String toString() {
            return "TsonFloatComplex{" +
                    "real=" + real +
                    ", image=" + image +
                    '}';
        }
    }
}
