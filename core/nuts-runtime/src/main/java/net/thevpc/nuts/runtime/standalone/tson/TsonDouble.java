package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonDouble extends TsonNumber {
    double value();
    TsonPrimitiveBuilder builder();
}
