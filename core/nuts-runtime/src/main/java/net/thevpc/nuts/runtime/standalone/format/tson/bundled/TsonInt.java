package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonInt extends TsonNumber {
    int value();
    TsonPrimitiveBuilder builder();
}
