package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonFormat {
    String format(TsonElement element);

    String format(TsonDocument element);

    TsonFormatBuilder builder();
}
