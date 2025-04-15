package net.thevpc.nuts.runtime.standalone.tson;

import java.math.BigDecimal;

public interface TsonBigComplex extends TsonNumber {
    BigDecimal real();

    BigDecimal imag();

    TsonPrimitiveBuilder builder();
}
