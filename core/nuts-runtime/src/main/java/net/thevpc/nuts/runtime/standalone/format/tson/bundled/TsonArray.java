package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonArray extends TsonListContainer, Iterable<TsonElement> {

    boolean isEmpty();

    int size();

    TsonArrayBuilder builder();

    TsonElement get(int index);
}
