package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.io.Reader;

public interface TsonParserFactory {
    String id();
    TsonStreamParser fromReader(Reader reader, String parser, Object source);
}
