package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonFloatComplex extends TsonNumber {
    float real();

    float imag();

    TsonPrimitiveBuilder builder();
}
