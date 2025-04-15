package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TsonBigComplexImpl extends AbstractNumberTsonElement implements TsonBigComplex {
    private BigDecimal real;
    private BigDecimal imag;

    public TsonBigComplexImpl(BigDecimal real, BigDecimal imag,String unit) {
        super(TsonElementType.BIG_COMPLEX, TsonNumberLayout.DECIMAL,unit);
        this.real = real;
        this.imag = imag;
    }

    @Override
    public Number numberValue() {
        return new TsonComplex(real, imag);
    }

    @Override
    public TsonBigDecimal toBigDecimal() {
        return new TsonBigDecimalImpl(real(), numberSuffix());
    }

    @Override
    public BigDecimal real() {
        return real;
    }

    @Override
    public BigDecimal imag() {
        return imag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonBigComplexImpl tsonInt = (TsonBigComplexImpl) o;
        return real.equals(tsonInt.real) && imag.equals(tsonInt.imag);
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
        return real();
    }

    @Override
    public BigInteger bigIntegerValue() {
        return real().toBigInteger();
    }


    public TsonBigInt toBigInt() {
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
        return Objects.hash(super.hashCode(), real, imag);
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return compare(this, o.toBigComplex());
    }

    public static int compare(TsonBigComplex a, TsonBigComplex oc) {
        int c = a.real().compareTo(oc.real());
        if (c != 0) {
            return c;
        }
        c = a.imag().compareTo(oc.imag());
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
                case DOUBLE:
                case BIG_DECIMAL:
                case BIG_COMPLEX: {
                    TsonBigComplex bce = o.toBigComplex();
                    int i = compare(this, o.toBigComplex());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
            }
        }
        return super.compareTo(o);
    }

    private static class TsonComplex extends Number {
        private BigDecimal real;
        private BigDecimal image;

        public TsonComplex(BigDecimal real, BigDecimal image) {
            this.real = real;
            this.image = image;
        }

        public BigDecimal getReal() {
            return real;
        }

        public BigDecimal getImage() {
            return image;
        }

        @Override
        public int intValue() {
            return real.intValue();
        }

        @Override
        public long longValue() {
            return real.longValue();
        }

        @Override
        public float floatValue() {
            return real.floatValue();
        }

        @Override
        public double doubleValue() {
            return real.doubleValue();
        }

        @Override
        public String toString() {
            return "TsonBigComplex{" +
                    "real=" + real +
                    ", image=" + image +
                    '}';
        }
    }
}
