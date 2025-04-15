package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.math.BigDecimal;

public interface TsonBigComplex extends TsonNumber {
    BigDecimal real();

    BigDecimal imag();

    TsonPrimitiveBuilder builder();
}
