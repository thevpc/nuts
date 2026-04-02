package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.reflect.NClassPairMultiMap;
import net.thevpc.nuts.util.NUplet;

import java.util.*;

public class NClassPairMultiMapImpl<A, B, V> implements NClassPairMultiMap<A, B, V> {
    private final NClassPairMapImpl<A, B, List<V>> base;

    public NClassPairMultiMapImpl(Class<? extends A> baseKey1Type, Class<? extends B> baseKey2Type, Class<V> valueType, boolean symmetric) {
        base = new NClassPairMapImpl<A, B, List<V>>(baseKey1Type, baseKey2Type, (Class) List.class, symmetric);
    }

    @Override
    public Set<NUplet<Class>> keySet() {
        return base.keySet();
    }

    @Override
    public void add(Class<? extends A> a, Class<? extends B> b, V value) {
        List<V> t = base.getExact(a, b);
        if (t == null) {
            t = new ArrayList<V>();
            base.put(a, b, t);
        }
        t.add(value);
    }

    @Override
    public void remove(Class<? extends A> a, Class<? extends B> b, V value) {
        List<V> t = base.getExact(a, b);
        if (t != null) {
            t.remove(value);
        }
    }

    @Override
    public boolean clear(Class<? extends A> a, Class<? extends B> b) {
        List<V> t = base.getExact(a, b);
        if (t != null) {
            return base.remove(a, b) != null;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean clear() {
        return base.clear();
    }

    @Override
    public List<V> get(Class<? extends A> a, Class<? extends B> b) {
        List<V> lv = base.get(a, b);
        if (lv != null) {
            return lv;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<V> findMatches(Class<? extends A> a, Class<? extends B> b) {
        ArrayList<V> all = new ArrayList<>();
        for (List<V> vs : base.findMatches(a, b)) {
            all.addAll(vs);
        }
        return all;
    }

    @Override
    public List<V> getExact(Class<? extends A> a, Class<? extends B> b) {
        List<V> t = base.getExact(a, b);
        if (t == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NClassPairMultiMapImpl)) return false;

        NClassPairMultiMapImpl that = (NClassPairMultiMapImpl) o;

        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return base != null ? base.hashCode() : 0;
    }
}
