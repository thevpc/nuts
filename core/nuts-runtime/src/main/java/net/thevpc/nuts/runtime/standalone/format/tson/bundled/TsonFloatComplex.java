package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonFloatComplex extends TsonNumber {
    float real();

    float imag();

    TsonPrimitiveBuilder builder();
}
