package net.thevpc.nuts.elem;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface NNumberElement extends NPrimitiveElement {
    Number numberValue();

    BigDecimal bigDecimalValue();

    BigInteger bigIntegerValue();

    NNumberLayout numberLayout();

    String numberSuffix();
}
