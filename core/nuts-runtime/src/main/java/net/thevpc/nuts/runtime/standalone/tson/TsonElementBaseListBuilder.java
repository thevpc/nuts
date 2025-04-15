package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface TsonElementBaseListBuilder {
    TsonElementBaseListBuilder addAll(TsonElement[] elements);

    TsonElementBaseListBuilder addAll(TsonElementBase[] elements);

    TsonElementBaseListBuilder addAll(Iterable<? extends TsonElementBase> elements);

    TsonElementBase getAt(int index);

    TsonElementBase get(String name);

    TsonElementBase get(TsonElementBase name);

    List<TsonElementBase> get2(String name);

    List<TsonElementBase> get2(TsonElementBase name);

    int size();


    TsonElementBaseListBuilder clear();

    TsonElementBaseListBuilder add(TsonElementBase key, TsonElementBase value);

    TsonElementBaseListBuilder add(String key, String value);

    TsonElementBaseListBuilder add(String key, int value);

    TsonElementBaseListBuilder add(String key, long value);

    TsonElementBaseListBuilder add(String key, float value);

    TsonElementBaseListBuilder add(String key, double value);

    TsonElementBaseListBuilder add(String key, byte value);

    TsonElementBaseListBuilder add(String key, short value);

    TsonElementBaseListBuilder add(String key, char value);

    TsonElementBaseListBuilder add(String key, Enum value);

    TsonElementBaseListBuilder add(String key, boolean value);

    TsonElementBaseListBuilder add(String key, TsonElementBase value);

    //////////////
    TsonElementBaseListBuilder set(TsonElementBase key, TsonElementBase value);

    TsonElementBaseListBuilder set(String key, String value);

    TsonElementBaseListBuilder set(String key, int value);

    TsonElementBaseListBuilder set(String key, long value);

    TsonElementBaseListBuilder set(String key, float value);

    TsonElementBaseListBuilder set(String key, double value);

    TsonElementBaseListBuilder set(String key, byte value);

    TsonElementBaseListBuilder set(String key, short value);

    TsonElementBaseListBuilder set(String key, char value);

    TsonElementBaseListBuilder set(String key, Enum value);

    TsonElementBaseListBuilder set(String key, boolean value);

    TsonElementBaseListBuilder set(String key, TsonElementBase value);

    //////////////
    TsonElementBaseListBuilder add(TsonElementBase element);

    TsonElementBaseListBuilder remove(TsonElementBase element);

    TsonElementBaseListBuilder remove(String name);

    TsonElementBaseListBuilder addAt(int index, TsonElementBase element);

    TsonElementBaseListBuilder removeAt(int index);
    TsonElementBaseListBuilder setAt(int index,TsonElementBase element);

    TsonElementBaseList build();

    Map<TsonElementBase, TsonElementBase> toBaseMap();

    Map<TsonElement, TsonElement> toMap();

    Map<TsonElementBase, List<TsonElementBase>> toMultiBaseMap();

    Map<TsonElement, List<TsonElement>> toMultiMap();

    List<TsonElementBase> toBaseList();

    List<TsonElement> toList();

    Iterable<TsonElement> toIterable();

    Iterator<TsonElement> toIterator();

    Iterable<TsonElementBase> toBaseIterable();

    Iterator<TsonElementBase> toBaseIterator();

    TsonElementBaseListBuilder ensureCapacity(int length);
}
