package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.io.*;

public interface TsonBinaryStream extends TsonElement {
    InputStream value();

    Reader getBase64Value();

    Reader getBase64Value(int lineMax);

    TsonPrimitiveBuilder builder();
}
