package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonDouble extends TsonNumber {
    double value();
    TsonPrimitiveBuilder builder();
}
