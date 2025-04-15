package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.time.LocalDate;

public interface TsonLocalDate extends TsonTemporal {
    LocalDate value();

    TsonPrimitiveBuilder builder();
}
