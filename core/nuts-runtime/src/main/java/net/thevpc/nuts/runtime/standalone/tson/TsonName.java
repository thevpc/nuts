package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonName extends TsonElement {
    String value();
    TsonPrimitiveBuilder builder();
}
