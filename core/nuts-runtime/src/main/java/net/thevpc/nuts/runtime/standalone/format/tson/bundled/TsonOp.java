package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonOp extends TsonElement {
    TsonOpType opType();

    TsonElement second();

    TsonElement first();

    String opName();

    TsonOpBuilder builder();
}
