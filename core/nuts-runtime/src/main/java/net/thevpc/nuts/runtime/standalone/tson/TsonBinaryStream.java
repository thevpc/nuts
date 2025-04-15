package net.thevpc.nuts.runtime.standalone.tson;

import java.io.*;

public interface TsonBinaryStream extends TsonElement {
    InputStream value();

    Reader getBase64Value();

    Reader getBase64Value(int lineMax);

    TsonPrimitiveBuilder builder();
}
