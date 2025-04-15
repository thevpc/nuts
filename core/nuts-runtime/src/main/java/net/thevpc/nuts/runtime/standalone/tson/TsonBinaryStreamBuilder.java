package net.thevpc.nuts.runtime.standalone.tson;

import java.io.InputStream;
import java.io.Reader;

public interface TsonBinaryStreamBuilder {
    TsonBinaryStream build();

    void writeBase64(String b64);
}
