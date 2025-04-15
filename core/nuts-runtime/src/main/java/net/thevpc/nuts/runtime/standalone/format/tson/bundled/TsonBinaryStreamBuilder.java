package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonBinaryStreamBuilder {
    TsonBinaryStream build();

    void writeBase64(String b64);
}
