package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonString extends TsonElement {
    String value();

    String raw();

    String literalString();

    TsonPrimitiveBuilder builder();
}
