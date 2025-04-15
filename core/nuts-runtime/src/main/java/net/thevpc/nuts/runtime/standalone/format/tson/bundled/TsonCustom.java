package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonCustom extends TsonElement {
    Object value();
    TsonCustomBuilder builder();
}
