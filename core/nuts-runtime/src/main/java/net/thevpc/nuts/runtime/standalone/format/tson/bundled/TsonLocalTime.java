package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.time.LocalTime;

public interface TsonLocalTime extends TsonTemporal {
    LocalTime value();

    TsonPrimitiveBuilder builder();
}
