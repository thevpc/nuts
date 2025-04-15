package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonDoubleComplex extends TsonNumber {
    double real();

    double imag();

    TsonPrimitiveBuilder builder();
}
