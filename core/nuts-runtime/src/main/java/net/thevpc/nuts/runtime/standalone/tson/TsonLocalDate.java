package net.thevpc.nuts.runtime.standalone.tson;

import java.time.LocalDate;

public interface TsonLocalDate extends TsonTemporal {
    LocalDate value();

    TsonPrimitiveBuilder builder();
}
