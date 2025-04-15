package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.math.BigInteger;

public interface TsonBigInt extends TsonNumber {
    BigInteger value();

    TsonPrimitiveBuilder builder();
}
