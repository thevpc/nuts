package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Taha BEN SALAH (taha.bensalah@gmail.com)
 * %creationtime 13 juil. 2006 22:14:21
 */
public class ClassMap<V> {

    private static Comparator<Class> CLASS_HIERARCHY_COMPARATOR = new Comparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            if (o1.isAssignableFrom(o2)) {
                return 1;
            } else if (o2.isAssignableFrom(o1)) {
                return -1;
            }
            if (o1.isInterface() && !o2.isInterface()) {
                return 1;
            }
            if (o2.isInterface() && !o1.isInterface()) {
                return -1;
            }
            return 0;
        }
    };

    private static final long serialVersionUID = 1L;
    private Class keyType;
    private Class<V> valueType;
    private HashMap<Class, V> values;
    private HashMap<Class, V[]> cachedValues;
    private HashMap<Class, Class[]> cachedHierarchy;

    public ClassMap(Class keyType, Class<V> valueType) {
        this(keyType, valueType, 0);
    }

    public ClassMap(Class keyType, Class<V> valueType, int initialCapacity) {
        this.keyType = keyType;
        this.valueType = valueType;
        values = new HashMap<Class, V>(initialCapacity);
        cachedValues = new HashMap<Class, V[]>(initialCapacity * 2);
        cachedHierarchy = new HashMap<Class, Class[]>(initialCapacity * 2);
    }

    public void putAll(Map<Class,V> other) {
        invalidateCache();
        values.putAll(other);
    }

    public void putAll(ClassMap<V> other) {
        invalidateCache();
        values.putAll(other.values);
    }

    public V put(Class classKey, V value) {
        invalidateCache();
        return values.put(classKey, value);
    }

    public V remove(Class classKey) {
        invalidateCache();
        return values.remove(classKey);
    }

    private void invalidateCache() {
        cachedValues.clear();
    }

    public Set<Class> keySet() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public Set<Map.Entry<Class, V>> entrySet() {
        return Collections.unmodifiableSet(values.entrySet());
    }

    public Class[] getKeys(Class classKey) {
        Class[] keis = cachedHierarchy.get(classKey);
        if (keis == null) {
            keis = findClassHierarchy(classKey, keyType);
            cachedHierarchy.put(classKey, keis);
        }
        return keis;
    }

    public V getRequired(Class key) {
        V[] found = getAllRequired(key);
        return found[0];
    }

    public V getExact(Class key) {
        return values.get(key);
    }

    public V get(Class key) {
        V[] found = getAll(key);
        if (found.length > 0) {
            return found[0];
        }
        return null;
    }

    public V[] getAllRequired(Class key) {
        V[] found = getAll(key);
        if (found.length > 0) {
            return found;
        }
        throw new NoSuchElementException(key.getName());
    }

    public V[] getAll(Class key) {
        V[] found = cachedValues.get(key);
        if (found == null) {
            Class[] keis = getKeys(key);
            List<V> all = new ArrayList<V>(keis.length);
            for (Class c : keis) {
                V u = values.get(c);
                if (u != null) {
                    all.add(u);
                }
            }
            found = all.toArray((V[]) Array.newInstance(valueType, all.size()));
            cachedValues.put(key, found);
        }
        return found;
    }


    public static Class[] findClassHierarchy(Class clazz, Class baseType) {
        HashSet<Class> seen = new HashSet<Class>();
        Queue<Class> queue = new LinkedList<Class>();
        List<Class> result = new LinkedList<Class>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            Class i = queue.remove();
            if (baseType == null || baseType.isAssignableFrom(i)) {
                if (!seen.contains(i)) {
                    seen.add(i);
                    result.add(i);
                    if (i.getSuperclass() != null) {
                        queue.add(i.getSuperclass());
                    }
                    for (Class ii : i.getInterfaces()) {
                        queue.add(ii);
                    }
                }
            }
        }
        Collections.sort(result, CLASS_HIERARCHY_COMPARATOR);
        return result.toArray(new Class[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMap)) return false;

        ClassMap classMap = (ClassMap) o;

        if (keyType != null ? !keyType.equals(classMap.keyType) : classMap.keyType != null) return false;
        if (valueType != null ? !valueType.equals(classMap.valueType) : classMap.valueType != null) return false;
        if (!Objects.equals(values, classMap.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        //transform map hashcode according to names and not class references
        if (values != null) {
            int h = 0;
            Iterator<Map.Entry<Class, V>> i = values.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Class, V> next = i.next();
                h += (next.getKey().getName().hashCode() ^ (next.getValue() == null ? 0 : next.getValue().hashCode()));
            }
            result = h;
        }
        result = 31 * result + (keyType != null ? keyType.getName().hashCode() : 0);
        result = 31 * result + (valueType != null ? valueType.getName().hashCode() : 0);
        return result;
    }
}
