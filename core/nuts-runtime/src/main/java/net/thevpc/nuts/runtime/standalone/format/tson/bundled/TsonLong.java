package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonLong extends TsonNumber {
    long value();

    TsonPrimitiveBuilder builder();
}
