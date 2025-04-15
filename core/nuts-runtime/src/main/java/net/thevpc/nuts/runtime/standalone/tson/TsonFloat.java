package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonFloat extends TsonNumber {
    float value();

    TsonPrimitiveBuilder builder();
}
