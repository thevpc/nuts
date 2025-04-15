package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonElementBase {
    TsonElementType type();

    TsonElement build();

    String toString();

    String toString(boolean compact);

    String toString(TsonFormat format);
}
