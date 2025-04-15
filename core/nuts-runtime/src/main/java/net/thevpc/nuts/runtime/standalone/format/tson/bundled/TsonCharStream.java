package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.io.Reader;

public interface TsonCharStream extends TsonElement {
    String getStreamType();

    Reader value();

    TsonPrimitiveBuilder builder();
}
