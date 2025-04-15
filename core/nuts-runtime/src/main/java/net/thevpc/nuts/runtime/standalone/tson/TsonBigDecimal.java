package net.thevpc.nuts.runtime.standalone.tson;

import java.math.BigDecimal;

public interface TsonBigDecimal extends TsonNumber {
    BigDecimal value();

    TsonPrimitiveBuilder builder();
}
