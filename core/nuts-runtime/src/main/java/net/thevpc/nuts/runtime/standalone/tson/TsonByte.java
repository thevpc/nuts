package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonByte extends TsonNumber {
    byte value();

    TsonPrimitiveBuilder builder();
}
