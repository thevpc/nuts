package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonString extends TsonElement {
    String value();

    String raw();

    String literalString();

    TsonPrimitiveBuilder builder();
}
