package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonObject extends TsonListContainer, Iterable<TsonElement> {
    boolean isNamed();

    boolean isParametrized();

    TsonElement get(String name);

    TsonElement get(TsonElement element);

    int size();

    TsonObjectBuilder builder();
}
