/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.collections;

import java.lang.reflect.Array;
import java.util.*;

/**
 * register parent class/interface and get value for all sub classes
 *
 * @author thevpc
 */
public class ClassMap<V> {

    private static final long serialVersionUID = 1L;
    private static final Comparator<Class> CLASS_HIERARCHY_COMPARATOR = new Comparator<Class>() {
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
    protected HashMap<Class, V> values;
    protected HashMap<Class, V[]> cachedValues;
    private final Class keyType;
    private final Class<V> valueType;
    private final HashMap<Class, Class[]> cachedHierarchy;

    public ClassMap(Class<V> valueType) {
        this(null, valueType);
    }

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
        return result.toArray(new Class[result.size()]);
    }

    public Set<Class<?>> cacheKeySet() {
        HashSet<Class<?>> r = new HashSet<>();
        r.addAll((Collection) this.values.keySet());
        r.addAll((Collection) cachedValues.keySet());
        return r;
    }

    public Set<V> allKeySet() {
        Set<Class> ks0 = values.keySet();
        HashSet u = new HashSet(ks0);
        for (Class a : ks0) {
            u.addAll(Arrays.asList(getKeys(a)));
        }
        return u;
    }

    public Set<V> keySet() {
        return new HashSet(values.keySet());
    }

    public Collection<V> values() {
        return values.values();
    }

    public V put(Class classKey, V value) {
        cachedValues.clear();
        return values.put(classKey, value);
    }

    public V remove(Class classKey) {
        cachedValues.clear();
        return values.remove(classKey);
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

    public boolean containsExactKey(Class key) {
        return values.containsKey(key);
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

    protected V[] getAllImpl(Class key) {
        Class[] keis = getKeys(key);
        List<V> all = new ArrayList<V>(keis.length);
        for (Class c : keis) {
            V u = values.get(c);
            if (u != null) {
                all.add(u);
            }
        }
        return all.toArray((V[]) Array.newInstance(valueType, 0));
    }

    public V[] getAll(Class key) {
        V[] found = cachedValues.get(key);
        if (found == null) {
            found = getAllImpl(key);
            cachedValues.put(key, found);
        }
        return found;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassMap)) {
            return false;
        }

        ClassMap classMap = (ClassMap) o;

        if (keyType != null ? !keyType.equals(classMap.keyType) : classMap.keyType != null) {
            return false;
        }
        if (valueType != null ? !valueType.equals(classMap.valueType) : classMap.valueType != null) {
            return false;
        }
        return values != null ? values.equals(classMap.values) : classMap.values == null;
    }

    public void clear() {
        values.clear();
        cachedValues.clear();
        cachedHierarchy.clear();
    }

    public int size() {
        return values.size();
    }

    public void expand() {
        for (Class k : values.keySet()) {
            getKeys(k);
        }
    }
}
