package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;

import java.util.*;

public class TsonElementBaseListImpl implements TsonElementBaseList {
    private ArrayList<TsonElementBase> elements = new ArrayList<>();

    public TsonElementBaseListImpl(ArrayList<TsonElementBase> elements) {
        this.elements = new ArrayList<>();
        for (TsonElementBase element : elements) {
            this.elements.add(Tson.ofElementBase(element));
        }
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
    public TsonElementBaseListBuilder builder() {
        return new TsonElementBaseListBuilderImpl(elements);
    }

    @Override
    public Iterator<TsonElementBase> iterator() {
        return elements.iterator();
    }

    @Override
    public Map<TsonElementBase, TsonElementBase> toMap() {
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
    public Map<TsonElementBase, List<TsonElementBase>> toMap2() {
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
    public List<TsonElementBase> toList() {
        return new ArrayList<>(elements);
    }
}
