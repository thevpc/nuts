package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonPair extends TsonElement {
    TsonElement value();
    TsonElement key();
    TsonPairBuilder builder();
}
