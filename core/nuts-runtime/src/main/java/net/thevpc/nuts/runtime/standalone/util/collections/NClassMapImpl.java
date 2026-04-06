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

import net.thevpc.nuts.reflect.NClassMap;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.reflect.NTypeNamePlatformDomain;
import net.thevpc.nuts.runtime.standalone.reflect.NReflectImpl;

import java.lang.reflect.Array;
import java.util.*;

/**
 * register parent class/interface and get value for all sub classes
 *
 * @author thevpc
 */
    public class NClassMapImpl<K, V> implements NClassMap<K, V> {

        private static final long serialVersionUID = 1L;

        protected Map<Class<? extends K>, V> values;
        protected Map<Class<? extends K>, V[]> cachedValues;
        private final Class keyType;
        private final Class<V> valueType;
        private final HashMap<Class, Class[]> cachedHierarchy;

        public NClassMapImpl(Class<V> valueType) {
            this(null, valueType);
        }

        public NClassMapImpl(Class<K> keyType, Class<V> valueType) {
            this(keyType, valueType, 0);
        }

        public NClassMapImpl(Class<K> keyType, Class<V> valueType, int initialCapacity) {
            this.keyType = keyType;
            this.valueType = valueType;
            values = new HashMap<Class<? extends K>, V>(initialCapacity);
            cachedValues = new HashMap<Class<? extends K>, V[]>(initialCapacity * 2);
            cachedHierarchy = new HashMap<Class, Class[]>(initialCapacity * 2);
        }


        public Set<Class<?>> cacheKeySet() {
            HashSet<Class<?>> r = new HashSet<>();
            r.addAll((Collection) this.values.keySet());
            r.addAll((Collection) cachedValues.keySet());
            return r;
        }

//        public Set<Class<? extends K>> allKeySet() {
//            Set<Class<? extends K>> ks0 = values.keySet();
//            Set<Class<? extends K>> u = new HashSet<>(ks0);
//            for (Class<? extends K> a : ks0) {
//                u.addAll(getSearchPath(a));
//            }
//            return u;
//        }

        public Set<Class<? extends K>> keySet() {
            return Collections.unmodifiableSet(values.keySet());
        }

        @Override
        public Set<Map.Entry<Class<? extends K>, V>> entrySet() {
            Set<Map.Entry<Class<? extends K>, V>> entries = values.entrySet();
            return Collections.unmodifiableSet((Set) entries);
        }

        public Collection<V> values() {
            return values.values();
        }

        public V put(Class<? extends K> classKey, V value) {
            cachedValues.clear();
            return values.put(classKey, value);
        }

        public V remove(Class<? extends K> classKey) {
            cachedValues.clear();
            return values.remove(classKey);
        }

        public List<Class<? extends K>> getSearchPath(Class<? extends K> classKey) {
            Class[] keis = cachedHierarchy.get(classKey);
            if (keis == null) {
                keis = NReflectUtils.findClassHierarchy(classKey, keyType, NReflectImpl.PLATFORM_DOMAIN);
                cachedHierarchy.put(classKey, keis);
            }
            return Arrays.asList(keis);
        }

        public boolean containsExactKey(Class<? extends K> key) {
            return values.containsKey(key);
        }

        public V getExact(Class<? extends K> key) {
            return values.get(key);
        }

        public V get(Class<? extends K> key) {
            List<V> found = findMatches(key);
            if (found.size() > 0) {
                return found.get(0);
            }
            return null;
        }

        protected V[] getAllImpl(Class<? extends K> key) {
            List<Class<? extends K>> keis = getSearchPath(key);
            List<V> all = new ArrayList<V>(keis.size());
            for (Class<? extends K> c : keis) {
                V u = values.get(c);
                if (u != null) {
                    all.add(u);
                }
            }
            return all.toArray((V[]) Array.newInstance(valueType, 0));
        }

        public List<V> findMatches(Class<? extends K> key) {
            V[] found = cachedValues.get(key);
            if (found == null) {
                found = getAllImpl(key);
                cachedValues.put(key, found);
            }
            return Arrays.asList(found);
        }

        @Override
        public int hashCode() {
            int result = 0;
            //transform map hashcode according to names and not class references
            if (values != null) {
                int h = 0;
                Iterator<Map.Entry<Class<? extends K>, V>> i = values.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<Class<? extends K>, V> next = i.next();
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
            if (!(o instanceof NClassMapImpl)) {
                return false;
            }

            NClassMapImpl classMap = (NClassMapImpl) o;

            if (!Objects.equals(keyType, classMap.keyType)) {
                return false;
            }
            if (!Objects.equals(valueType, classMap.valueType)) {
                return false;
            }
            return Objects.equals(values, classMap.values);
        }

        public void clear() {
            values.clear();
            cachedValues.clear();
            cachedHierarchy.clear();
        }

        @Override
        public boolean isEmpty() {
            return values.isEmpty();
        }

        public int size() {
            return values.size();
        }

        public void expand() {
            for (Class k : values.keySet()) {
                getSearchPath(k);
            }
        }
    }
