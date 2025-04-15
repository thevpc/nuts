package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.List;
import java.util.Map;

public interface TsonElementBaseList extends Iterable<TsonElementBase> {
    TsonElementBase getAt(int index);

    TsonElementBase get(String name);

    TsonElementBase get(TsonElementBase name);

    List<TsonElementBase> get2(String name);

    List<TsonElementBase> get2(TsonElementBase name);

    Map<TsonElementBase, TsonElementBase> toMap();

    Map<TsonElementBase, List<TsonElementBase>> toMap2();

    List<TsonElementBase> toList();

    int size();

    TsonElementBaseListBuilder builder();
}
