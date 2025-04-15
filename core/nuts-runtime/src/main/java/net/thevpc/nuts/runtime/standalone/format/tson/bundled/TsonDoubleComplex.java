package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonDoubleComplex extends TsonNumber {
    double real();

    double imag();

    TsonPrimitiveBuilder builder();
}
