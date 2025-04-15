package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonAlias extends TsonElement {
    String getName();
    TsonPrimitiveBuilder builder();
}
