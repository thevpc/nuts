package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.reflect.NClassPairMap;
import net.thevpc.nuts.reflect.NReflect;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.reflect.NTypeNamePlatformDomain;
import net.thevpc.nuts.util.NUplet;

import java.lang.reflect.Array;
import java.util.*;

public class NClassPairMapImpl<A, B, V> implements NClassPairMap<A, B, V> {
    private static final long serialVersionUID = 1L;
    private final boolean symmetric;
    private final Class<? extends A> baseKey1Type;
    private final Class<? extends B> baseKey2Type;
    private final Class<V> valueType;
    private final HashMap<NUplet<Class>, V> values = new HashMap<NUplet<Class>, V>();
    private final HashMap<NUplet<Class>, V[]> cachedValues = new HashMap<NUplet<Class>, V[]>();
    private final HashMap<NUplet<Class>, NUplet<Class>[]> cachedHierarchy = new HashMap<NUplet<Class>, NUplet<Class>[]>();

    public NClassPairMapImpl(Class<? extends A> baseKey1Type, Class<? extends B> baseKey2Type, Class<V> valueType, boolean symmetric) {
        this.baseKey1Type = baseKey1Type;
        this.baseKey2Type = baseKey2Type;
        this.valueType = valueType;
        this.symmetric = symmetric;
    }

    @Override
    public Set<NUplet<Class>> keySet() {
        return values.keySet();
    }

    public boolean clear() {
        boolean result = !values.isEmpty();
        values.clear();
        cachedValues.clear();
        return result;
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public V put(Class<? extends A> classKey1, Class<? extends B> classKey2, V value) {
        cachedValues.clear();
        return values.put(createKey(classKey1, classKey2), value);
    }

    @Override
    public V remove(Class<? extends A> classKey1, Class<? extends B> classKey2) {
        cachedValues.clear();
        return values.remove(createKey(classKey1, classKey2));
    }

    @Override
    public List<NUplet<Class>> getSearchPath(Class<? extends A> classKey1, Class<? extends B> classKey2) {
        return Arrays.asList(getKeys(createKey(classKey1, classKey2)));
    }

    private NUplet<Class>[] getKeys(NUplet<Class> classKey) {
        NUplet<Class>[] keis = cachedHierarchy.get(classKey);
        if (keis == null) {
            keis = evalHierarchy(classKey);
            cachedHierarchy.put(classKey, keis);
        }
        return keis;
    }


    @Override
    public V getExact(Class<? extends A> classKey1, Class<? extends B> classKey2) {
        return values.get(createKey(classKey1, classKey2));
    }

    @Override
    public V get(Class<? extends A> classKey1, Class<? extends B> classKey2) {
        return get(createKey(classKey1, classKey2));
    }

    private V get(NUplet<Class> key) {
        List<V> found = getAll(key);
        if (!found.isEmpty()) {
            return found.get(0);
        }
        return null;
    }

    @Override
    public List<V> findMatches(Class<? extends A> classKey1, Class<? extends B> classKey2) {
        return getAll(createKey(classKey1, classKey2));
    }

    private List<V> getAll(NUplet<Class> classKey) {
        V[] found = cachedValues.get(classKey);
        if (found == null) {
            NUplet<Class>[] keis = getKeys(classKey);
            List<V> all = new ArrayList<V>();
            for (NUplet<Class> c : keis) {
                V u = values.get(c);
                if (u != null) {
                    all.add(u);
                }
            }
            found = all.toArray((V[]) Array.newInstance(valueType, all.size()));
            cachedValues.put(classKey, found);
        }
        return Arrays.asList(found);
    }


    public NUplet<Class>[] evalHierarchy(NUplet<Class> clazz) {
        Class[] first = NReflectUtils.findClassHierarchy(clazz.get(0), baseKey1Type, NTypeNamePlatformDomain.of());
        Class[] second = NReflectUtils.findClassHierarchy(clazz.get(1), baseKey2Type, NTypeNamePlatformDomain.of());
        HashSet<NUplet<Class>> seen = new HashSet<NUplet<Class>>();
        List<IndexComparable<NUplet<Class>>> result = new LinkedList<IndexComparable<NUplet<Class>>>();
        for (int i1 = 0; i1 < first.length; i1++) {
            Class aClass = first[i1];
            for (int i2 = 0; i2 < second.length; i2++) {
                Class bClass = second[i2];
                NUplet<Class> i = createKey(aClass, bClass);
                if (!seen.contains(i)) {
                    seen.add(i);
                    result.add(new IndexComparable(i1 + i2, i));
                }
            }
        }
        Collections.sort(result);
        List<NUplet<Class>> result2 = new ArrayList<NUplet<Class>>(result.size());
        for (IndexComparable<NUplet<Class>> ic : result) {
            result2.add(ic.v);
        }
        return result2.toArray(new NUplet[result.size()]);
    }

    private static class IndexComparable<T> implements Comparable<IndexComparable> {
        int i;
        T v;

        @Override
        public int compareTo(IndexComparable o) {
            return i - o.i;
        }

        private IndexComparable(int i, T v) {
            this.i = i;
            this.v = v;
        }
    }

    protected NUplet<Class> createKey(Class first, Class second) {
        if (symmetric) {
            int c = first.getName().compareTo(second.getName());
            if (c > 0) {
                Class p = second;
                second = first;
                first = p;
            }
        }
        return NUplet.of(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NClassPairMapImpl)) return false;

        NClassPairMapImpl that = (NClassPairMapImpl) o;

        if (symmetric != that.symmetric) return false;
        if (!Objects.equals(baseKey1Type, that.baseKey1Type)) return false;
        if (!Objects.equals(baseKey2Type, that.baseKey2Type)) return false;
        if (!Objects.equals(valueType, that.valueType)) return false;
        return values != null ? values.equals(that.values) : that.values == null;
    }

    @Override
    public int hashCode() {
        int result = (symmetric ? 1 : 0);
        result = 31 * result + (baseKey1Type != null ? baseKey1Type.hashCode() : 0);
        result = 31 * result + (baseKey2Type != null ? baseKey2Type.hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }


}
