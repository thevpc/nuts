package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonParametrizedElement extends TsonElement {
    boolean isParametrized();

    TsonElementList params();

    int paramsCount();
}
