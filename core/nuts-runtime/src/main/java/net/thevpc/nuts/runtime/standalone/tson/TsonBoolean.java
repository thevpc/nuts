package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonBoolean extends TsonElement {
    boolean value();
    TsonPrimitiveBuilder builder();
}
