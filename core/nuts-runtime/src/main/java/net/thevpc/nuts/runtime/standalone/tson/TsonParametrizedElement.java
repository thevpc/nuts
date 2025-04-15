package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonParametrizedElement extends TsonElement {
    boolean isParametrized();

    TsonElementList params();

    int paramsCount();
}
