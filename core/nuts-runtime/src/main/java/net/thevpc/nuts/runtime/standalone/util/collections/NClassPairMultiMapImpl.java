package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.reflect.NClassPairMultiMap;
import net.thevpc.nuts.util.NUplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NClassPairMultiMapImpl<A,B,V> implements NClassPairMultiMap<A,B,V> {
    private NClassPairMapImpl<A,B,List<V>> base;

    public NClassPairMultiMapImpl(Class<? extends A> baseKey1Type, Class<? extends B> baseKey2Type, Class<V> valueType, boolean symmetric) {
        base = new NClassPairMapImpl<A,B,List<V>>(baseKey1Type, baseKey2Type, (Class) List.class, symmetric);
    }

    @Override
    public Set<NUplet<Class>> keySet(){
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
    public void clear(Class<? extends A> a, Class<? extends B> b) {
        List<V> t = base.getExact(a, b);
        if (t != null) {
            base.remove(a, b);
        }
    }


    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public List<V> get(Class<? extends A> a, Class<? extends B> b) {
        List<V> lv = base.get(a, b);
        if(lv!=null){
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

        if (base != null ? !base.equals(that.base) : that.base != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return base != null ? base.hashCode() : 0;
    }
}
