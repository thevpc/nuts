package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.time.LocalDateTime;

public interface TsonLocalDateTime extends TsonTemporal {
    LocalDateTime value();

    TsonPrimitiveBuilder builder();
}
