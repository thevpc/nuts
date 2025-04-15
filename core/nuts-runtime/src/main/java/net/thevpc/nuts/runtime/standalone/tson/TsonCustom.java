package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonCustom extends TsonElement {
    Object value();
    TsonCustomBuilder builder();
}
