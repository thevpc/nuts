package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.time.Instant;

public interface TsonInstant extends TsonTemporal {
    Instant value();

    TsonPrimitiveBuilder builder();
}
