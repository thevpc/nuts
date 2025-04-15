package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonFloat extends TsonNumber {
    float value();

    TsonPrimitiveBuilder builder();
}
