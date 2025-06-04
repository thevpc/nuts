package net.thevpc.nuts.runtime.standalone.format.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultNNumberElement extends DefaultNPrimitiveElement implements NNumberElement {
    private NNumberLayout layout;
    private String suffix;

    public DefaultNNumberElement(NElementType type, Number value) {
        this(type, value, null, null, null, null);
    }

    public DefaultNNumberElement(NElementType type, Number value, NNumberLayout layout, String suffix) {
        this(type, value, layout, suffix, null, null);
    }

    public DefaultNNumberElement(NElementType type, Number value,
                                 NNumberLayout layout,
                                 String suffix,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
        this.layout = layout == null ? NNumberLayout.DECIMAL : layout;
        this.suffix = NStringUtils.trimToNull(suffix);
    }

    @Override
    public Number numberValue() {
        return (Number) value();
    }

    @Override
    public NNumberLayout numberLayout() {
        return layout;
    }

    @Override
    public String numberSuffix() {
        return suffix;
    }

    @Override
    public NOptional<NNumberElement> asNumber() {
        return NOptional.of(this);
    }

    @Override
    public BigDecimal bigDecimalValue() {
        Number d = numberValue();
        if (d instanceof BigDecimal) {
            return (BigDecimal) d;
        }
        if (d instanceof BigInteger) {
            return new BigDecimal(d.toString());
        }
        return BigDecimal.valueOf(d.doubleValue());
    }

    @Override
    public BigInteger bigIntegerValue() {
        Number d = numberValue();
        if (d instanceof BigDecimal) {
            return ((BigDecimal) d).toBigInteger();
        }
        if (d instanceof BigInteger) {
            return (BigInteger) d;
        }
        return BigInteger.valueOf(d.longValue());
    }
}
