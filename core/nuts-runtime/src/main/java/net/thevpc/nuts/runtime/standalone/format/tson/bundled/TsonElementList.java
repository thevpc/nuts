package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.List;
import java.util.Map;

public interface TsonElementList extends Iterable<TsonElement> {
    TsonElement getAt(int index);

    TsonElement get(String name);

    TsonElement get(TsonElement name);

    Map<TsonElement, TsonElement> toMap();

    Map<TsonElement, List<TsonElement>> toMultiMap();

    List<TsonElement> toList();

    TsonElement[] toArray();

    List<TsonElement> getValues(String name);

    List<TsonElement> getValues(TsonElement name);

    int size();

    TsonElementBaseListBuilder builder();

    boolean isEmpty();
}
