package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonShort extends TsonNumber {
    short value();
    TsonPrimitiveBuilder builder();
}
