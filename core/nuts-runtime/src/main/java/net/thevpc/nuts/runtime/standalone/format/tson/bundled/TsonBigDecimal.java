package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.math.BigDecimal;

public interface TsonBigDecimal extends TsonNumber {
    BigDecimal value();

    TsonPrimitiveBuilder builder();
}
