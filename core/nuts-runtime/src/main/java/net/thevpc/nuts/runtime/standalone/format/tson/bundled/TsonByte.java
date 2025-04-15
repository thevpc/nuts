package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonByte extends TsonNumber {
    byte value();

    TsonPrimitiveBuilder builder();
}
