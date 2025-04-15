package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonFormat {
    String format(TsonElement element);

    String format(TsonDocument element);

    TsonFormatBuilder builder();
}
