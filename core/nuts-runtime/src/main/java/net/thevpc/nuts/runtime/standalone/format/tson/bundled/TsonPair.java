package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonPair extends TsonElement {
    TsonElement value();
    TsonElement key();
    TsonPairBuilder builder();
}
