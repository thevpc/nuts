package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonElementBaseListBuilderImpl;

import java.util.*;
import java.util.stream.Collectors;

public class TsonElementListImpl implements TsonElementList {
    private ArrayList<TsonElement> elements = new ArrayList<>();

    public TsonElementListImpl(List<TsonElementBase> elements) {
        this.elements = new ArrayList<>();
        for (TsonElementBase element : elements) {
            this.elements.add(Tson.ofElement(element));
        }
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public TsonElement getAt(int index) {
        return elements.get(index);
    }

    @Override
    public TsonElement get(String name) {
        return get(name == null ? null : Tson.ofString(name));
    }

    @Override
    public TsonElement get(TsonElement name) {
        TsonElement vn = (name == null ? Tson.ofNull() : name).build();
        for (TsonElement element : elements) {
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
    public List<TsonElement> getValues(String name) {
        return getValues(name == null ? null : Tson.ofString(name));
    }

    @Override
    public List<TsonElement> getValues(TsonElement name) {
        TsonElement vn = (name == null ? Tson.ofNull() : name).build();
        List<TsonElement> all = new ArrayList<>();
        LinkedHashMap<TsonElement, TsonElement> m = new LinkedHashMap<>();
        for (TsonElement element : elements) {
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
        return new TsonElementBaseListBuilderImpl(
                elements.stream().map(x -> x).collect(Collectors.toList())
        );
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return elements.iterator();
    }

    @Override
    public Map<TsonElement, TsonElement> toMap() {
        LinkedHashMap<TsonElement, TsonElement> m = new LinkedHashMap<>();
        for (TsonElement element : elements) {
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
    public Map<TsonElement, List<TsonElement>> toMultiMap() {
        LinkedHashMap<TsonElement, List<TsonElement>> m = new LinkedHashMap<>();
        for (TsonElement element : elements) {
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
    public List<TsonElement> toList() {
        return new ArrayList<>(elements);
    }

    @Override
    public TsonElement[] toArray() {
        return elements.toArray(new TsonElement[0]);
    }
}
