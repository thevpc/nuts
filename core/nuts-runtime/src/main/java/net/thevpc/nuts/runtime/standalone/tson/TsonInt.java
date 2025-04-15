package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonInt extends TsonNumber {
    int value();
    TsonPrimitiveBuilder builder();
}
