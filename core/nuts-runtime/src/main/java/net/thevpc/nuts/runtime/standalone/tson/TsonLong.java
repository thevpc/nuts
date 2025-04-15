package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonLong extends TsonNumber {
    long value();

    TsonPrimitiveBuilder builder();
}
