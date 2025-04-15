package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;

import java.util.*;
import java.util.stream.Collectors;

public class TsonElementBaseListBuilderImpl implements TsonElementBaseListBuilder {
    private ArrayList<TsonElementBase> elements = new ArrayList<>();

    public TsonElementBaseListBuilderImpl() {

    }

    public TsonElementBaseListBuilderImpl(List<TsonElementBase> elements) {
        addAll(elements);
    }

    public TsonElementBaseListBuilder set(String key, TsonElementBase value) {
        return set(Tson.ofString(key), Tson.ofElementBase(value));
    }

    public TsonElementBaseListBuilder set(TsonElementBase key, TsonElementBase value) {
        key = Tson.of(key);
        value = Tson.of(value);
        for (int i = 0; i < elements.size(); i++) {
            TsonElementBase e = elements.get(i);
            if (e.type() == TsonElementType.PAIR) {
                TsonElement k = ((TsonPair) e).key();
                if ((key == null || key.type() == TsonElementType.NULL)) {
                    if (k.type() == TsonElementType.NULL) {
                        elements.set(i, Tson.ofPair(key, value));
                        return this;
                    }
                } else {
                    if (k.build().equals(key.build())) {
                        elements.set(i, Tson.ofPair(key, value));
                        return this;
                    }
                }
            }
        }
        add(Tson.ofPair(key, value));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder clear() {
        elements.clear();
        return this;
    }


    @Override
    public TsonElementBaseListBuilder add(TsonElementBase key, TsonElementBase value) {
        return add(Tson.ofPair(key, value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, TsonElementBase value) {
        return add(Tson.ofPair(key, value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, String value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, int value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, long value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, float value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, double value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, byte value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, short value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, char value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, Enum value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder add(String key, boolean value) {
        return add(key, Tson.of(value));
    }

    //////////////

    @Override
    public TsonElementBaseListBuilder set(String key, String value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, int value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, long value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, float value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, double value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, byte value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, short value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, char value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, Enum value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonElementBaseListBuilder set(String key, boolean value) {
        return set(key, Tson.of(value));
    }

    //////////////
    @Override
    public TsonElementBaseListBuilder add(TsonElementBase element) {
        elements.add(Tson.of(element));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder remove(TsonElementBase element) {
        elements.remove(Tson.of(element));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder remove(String name) {
        elements.remove(Tson.ofString(name));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder addAt(int index, TsonElementBase element) {
        elements.add(index, Tson.of(element));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder setAt(int index, TsonElementBase element) {
        elements.set(index, Tson.of(element));
        return this;
    }

    @Override
    public TsonElementBaseListBuilder removeAt(int index) {
        elements.remove(index);
        return this;
    }

    @Override
    public TsonElementBaseList build() {
        return new TsonElementBaseListImpl(elements);
    }

    @Override
    public TsonElementBaseListBuilder addAll(TsonElement[] elements) {
        if (elements != null) {
            for (TsonElementBase element : elements) {
                if (element != null) {
                    this.elements.add(element);
                }
            }
        }
        return this;
    }

    @Override
    public TsonElementBaseListBuilder addAll(TsonElementBase[] elements) {
        if (elements != null) {
            for (TsonElementBase element : elements) {
                if (element != null) {
                    this.elements.add(element);
                }
            }
        }
        return this;
    }

    @Override
    public TsonElementBaseListBuilder addAll(Iterable<? extends TsonElementBase> elements) {
        if (elements != null) {
            for (TsonElementBase element : elements) {
                if (element != null) {
                    this.elements.add(element);
                }
            }
        }
        return this;
    }

    @Override
    public TsonElementBase getAt(int index) {
        return elements.get(index);
    }

    @Override
    public TsonElementBase get(String name) {
        return get(name == null ? null : Tson.ofString(name));
    }

    @Override
    public TsonElementBase get(TsonElementBase name) {
        TsonElement vn = (name == null ? Tson.ofNull() : name).build();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                if (Objects.equals(element1.key().build(), vn)) {
                    return element1.value();
                }
            } else {
                if (Objects.equals(element.build(), vn)) {
                    return element;
                }
            }
        }
        return null;
    }

    @Override
    public List<TsonElementBase> get2(String name) {
        return get2(name == null ? null : Tson.ofString(name));
    }

    @Override
    public List<TsonElementBase> get2(TsonElementBase name) {
        TsonElement vn = (name == null ? Tson.ofNull() : name).build();
        List<TsonElementBase> all = new ArrayList<>();
        LinkedHashMap<TsonElementBase, TsonElementBase> m = new LinkedHashMap<>();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                if (Objects.equals(element1.key().build(), vn)) {
                    all.add(element1.value());
                }
            } else {
                if (Objects.equals(element.build(), vn)) {
                    all.add(element);
                }
            }
        }
        return all;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Map<TsonElementBase, TsonElementBase> toBaseMap() {
        LinkedHashMap<TsonElementBase, TsonElementBase> m = new LinkedHashMap<>();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                m.put(element1.key(), element1.value());
            } else {
                m.put(element, element);
            }
        }
        return m;
    }

    @Override
    public Map<TsonElement, TsonElement> toMap() {
        LinkedHashMap<TsonElement, TsonElement> m = new LinkedHashMap<>();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                m.put(element1.key(), element1.value());
            } else {
                m.put(element.build(), element.build());
            }
        }
        return m;
    }

    @Override
    public Map<TsonElementBase, List<TsonElementBase>> toMultiBaseMap() {
        LinkedHashMap<TsonElementBase, List<TsonElementBase>> m = new LinkedHashMap<>();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                m.computeIfAbsent(element1.key(), k -> new ArrayList<>()).add(element1.value());
            } else {
                m.computeIfAbsent(element, k -> new ArrayList<>()).add(element);
            }
        }
        return m;
    }

    @Override
    public Map<TsonElement, List<TsonElement>> toMultiMap() {
        LinkedHashMap<TsonElement, List<TsonElement>> m = new LinkedHashMap<>();
        for (TsonElementBase element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                m.computeIfAbsent(element1.key(), k -> new ArrayList<>()).add(element1.value());
            } else {
                m.computeIfAbsent(element.build(), k -> new ArrayList<>()).add(element.build());
            }
        }
        return m;
    }

    @Override
    public List<TsonElementBase> toBaseList() {
        return new ArrayList<>(elements);
    }

    @Override
    public List<TsonElement> toList() {
        return elements.stream().map(x -> x.build()).collect(Collectors.toList());
    }

    @Override
    public Iterable<TsonElement> toIterable() {
        return this::toIterator;
    }

    @Override
    public Iterator<TsonElement> toIterator() {
        Iterator<TsonElementBase> i = elements.iterator();
        return new Iterator<TsonElement>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public TsonElement next() {
                return i.next().build();
            }
        };
    }

    @Override
    public Iterable<TsonElementBase> toBaseIterable() {
        return () -> elements.iterator();
    }

    @Override
    public Iterator<TsonElementBase> toBaseIterator() {
        return elements.iterator();
    }

    @Override
    public TsonElementBaseListBuilder ensureCapacity(int length) {
        elements.ensureCapacity(length);
        return this;
    }
}
