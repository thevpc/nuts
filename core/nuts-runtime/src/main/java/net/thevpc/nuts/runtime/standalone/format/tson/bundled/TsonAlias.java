package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonAlias extends TsonElement {
    String getName();
    TsonPrimitiveBuilder builder();
}
