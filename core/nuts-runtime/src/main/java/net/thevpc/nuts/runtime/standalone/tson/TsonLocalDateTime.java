package net.thevpc.nuts.runtime.standalone.tson;

import java.time.LocalDateTime;

public interface TsonLocalDateTime extends TsonTemporal {
    LocalDateTime value();

    TsonPrimitiveBuilder builder();
}
