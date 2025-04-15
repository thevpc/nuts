package net.thevpc.nuts.runtime.standalone.tson;

import java.time.LocalTime;

public interface TsonLocalTime extends TsonTemporal {
    LocalTime value();

    TsonPrimitiveBuilder builder();
}
