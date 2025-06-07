package net.thevpc.nuts.util;

import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class NPlatformSignatureMap<V> {
    private final Map<Integer, NSigMapBySize<V>> map = new HashMap<>();
    private Class<V> valueType;

    public NPlatformSignatureMap(Class<V> valueType) {
        this.valueType = valueType;
    }

    public void putMulti(NPlatformSignature sig, V value, NPlatformSignature... sigs) {
        NAssert.requireNonNull(sig);
        synchronized (map) {
            int usize = sig.size();
            NSigMapBySize<V> m = map.get(usize);
            if (m == null) {
                m = new NSigMapBySize<>(usize);
                map.put(usize, m);
            }
            m.put(sig, value);
            if (sigs != null) {
                for (NPlatformSignature nSig : sigs) {
                    if (nSig != null) {
                        m.put(nSig, value);
                    }
                }
            }
        }
    }

    public void put(NPlatformSignature sig, V value) {
        NAssert.requireNonNull(sig);
        synchronized (map) {
            int usize = sig.size();
            NSigMapBySize<V> m = map.get(usize);
            if (m == null) {
                m = new NSigMapBySize<>(usize);
                map.put(usize, m);
            }
            m.put(sig, value);
        }
    }

    public NOptional<V> get(NPlatformSignature sig) {
        NAssert.requireNonNull(sig);
        synchronized (map) {
            NSigMapBySize<V> m = map.get(sig.size());
            if (m == null) {
                return NOptional.ofNamedEmpty(NMsg.ofC("%s", sig));
            }
            return m.get(sig);
        }
    }

    public void remove(NPlatformSignature sig) {
        NAssert.requireNonNull(sig);
        synchronized (map) {
            NSigMapBySize<V> m = map.get(sig.size());
            if (m == null) {
                return;
            }
            m.remove(sig);
        }
    }

    public Map<NPlatformSignature, V> toMap() {
        Map<NPlatformSignature, V> all = new HashMap<>();
        for (Map.Entry<Integer, NSigMapBySize<V>> e : map.entrySet()) {
            all.putAll(e.getValue().map.toMap());
        }
        return all;
    }

    public int size() {
        int s = 0;
        for (Map.Entry<Integer, NSigMapBySize<V>> e : map.entrySet()) {
            s += e.getValue().map.size();
        }
        return s;
    }

    private static class NSigMapBySize<V> {
        private NOptionalMap<NPlatformSignature, V> map = new NOptionalMap<>();
        private int count;
        private boolean invalidCache;
        private Map<NPlatformSignature, ValueWithDistance<V>> cache = new HashMap<>();
        private Set<NPlatformSignature> findInProgress = new HashSet<>();

        public NSigMapBySize(int count) {
            this.count = count;
        }

        public NOptional<V> get(NPlatformSignature sig) {
            if (invalidCache) {
                NOptional<V> v = map.get(sig);
                if (v.isPresent()) {
                    return v;
                }
                cache.clear();
            } else {
                ValueWithDistance<V> o = cache.get(sig);
                if (o != null) {
                    return NOptional.ofNullable(o.getValueNonAmbiguous());
                }
                NOptional<V> v = map.get(sig);
                if (v.isPresent()) {
                    cache.put(sig, ValueWithDistance.ofSimple(sig, sig, 0, v.get()));
                    return v;
                }
            }
            ValueWithDistance<V> vd = find(sig, 0);
            if (vd == null) {
                cache.put(sig, vd = ValueWithDistance.ofEmpty(sig));
            }
            return NOptional.ofNullable(vd.getValueNonAmbiguous());
        }

        public ValueWithDistance<V> find(NPlatformSignature sig, int distance) {
            if (findInProgress.contains(sig)) {
                return null;
            }
            try {
                findInProgress.add(sig);
                NOptional<V> v = map.get(sig);
                if (v.isPresent()) {
                    return ValueWithDistance.ofSimple(sig, sig, distance, v.get());
                }
                ValueWithDistanceBestResolver<V> bestResolver = new ValueWithDistanceBestResolver<>();
                for (int i = 0; i < sig.size(); i++) {
                    Type t = sig.getType(i);
                    if (t instanceof Class) {
                        Class c = (Class) t;
                        findClassAtPos(sig, i, c, bestResolver, distance);
                    } else if (t instanceof GenericArrayType) {
                        //TODO
                    } else if (t instanceof TypeVariable<?>) {
                        //TODO
                    } else if (t instanceof ParameterizedType) {
                        //TODO
                    } else if (t instanceof ParameterizedType) {
                        //TODO
                    } else if (t instanceof WildcardType) {
                        //TODO
                    }
                }
                ValueWithDistance<V> r = ValueWithDistance.ofPointers(sig, distance, bestResolver.best);
                return r;
            } finally {
                findInProgress.remove(sig);
            }
        }

        private void findClassAtPos(NPlatformSignature sig, int i, Class c, ValueWithDistanceBestResolver<V> bestResolver, int distance) {
            if (c.isPrimitive()) {
                NOptional<Class<?>> bt = NReflectUtils.toBoxedType(c);
                ValueWithDistance<V> vd = find(sig.set(bt.get(), i), distance + 1);
                bestResolver.add(vd);
            } else {
                NOptional<Class<?>> bt = NReflectUtils.toPrimitiveType(c);
                if (bt.isPresent()) {
                    ValueWithDistance<V> vd = find(sig.set(bt.get(), i), distance + 1);
                    bestResolver.add(vd);
                }
            }
            Class sc = c.getSuperclass();
            if (sc != null) {
                ValueWithDistance<V> vd = find(sig.set(sc, i), distance + 2);
                bestResolver.add(vd);
            }
            Class[] superInterfaces = c.getInterfaces();
            for (Class si : superInterfaces) {
                ValueWithDistance<V> vd = find(sig.set(si, i), distance + 2);
                bestResolver.add(vd);
            }
        }

        public NOptional<V> remove(NPlatformSignature uplet) {
            NOptional<V> r = map.remove(uplet);
            if (r.isPresent()) {
                invalidCache = true;
            }
            return r;
        }

        public void put(NPlatformSignature uplet, V value) {
            NOptional<V> o = map.put(uplet, value);
            if (!o.isPresent() || !Objects.equals(o.orNull(), value)) {
                invalidCache = true;
            }
        }

    }

    private static class ValueWithDistanceBestResolver<V> {
        List<ValueWithDistance<V>> best = new ArrayList<>();
        int bestDistance = -1;

        public void add(ValueWithDistance<V> vd) {
            if (vd != null && vd.isPresent()) {
                if (bestDistance < 0 || bestDistance > vd.distance) {
                    bestDistance = vd.distance;
                    best.clear();
                    if (vd.pointers == null) {
                        best.add(vd);
                    } else {
                        best.addAll(vd.pointers.stream().map(x -> ValueWithDistance.ofSimple(vd.key, vd.effKey, vd.distance, x.value)).collect(Collectors.toList()));
                    }
                } else if (bestDistance == vd.distance) {
                    if (vd.pointers == null) {
                        best.add(vd);
                    } else {
                        best.addAll(vd.pointers.stream().map(x -> ValueWithDistance.ofSimple(vd.key, vd.effKey, vd.distance, x.value)).collect(Collectors.toList()));
                    }
                }
            }
        }
    }

    private static class ValueWithDistance<V> {
        NPlatformSignature key;
        NPlatformSignature effKey;
        V value;
        List<ValueWithDistance<V>> pointers;
        int distance;
        boolean empty;

        public static <V> ValueWithDistance<V> ofSimple(NPlatformSignature key, NPlatformSignature effKey, int distance, V value) {
            return new ValueWithDistance<V>(
                    key, effKey, value, null, distance, false
            );
        }

        public static <V> ValueWithDistance<V> ofPointers(NPlatformSignature key, int distance, List<ValueWithDistance<V>> values) {
            if (values.size() == 0) {
                return ofEmpty(key);
            }
            V value = null;
            NPlatformSignature effKey1 = key;
            if (values.size() == 1) {
                value = values.get(0).value;
                effKey1 = values.get(0).effKey;
            }
            //remove duplicates if the very same impl (aka effective key)
            if (values.size() > 1) {
                LinkedHashMap<NPlatformSignature, ValueWithDistance<V>> valuesMap = new LinkedHashMap<>();
                for (ValueWithDistance<V> v : values) {
                    if (!valuesMap.containsKey(v.effKey)) {
                        valuesMap.put(v.effKey, v);
                    }
                }
                values = new ArrayList<>(valuesMap.values());
            }
            //remove duplicates if the very same impl (aka function impl)
            if (values.size() > 1) {
                Set<Object> fcts = new HashSet<>();
                ArrayList<ValueWithDistance<V>> newValues = new ArrayList<>();
                for (ValueWithDistance<V> v : values) {
                    if (!fcts.contains(v.value)) {
                        fcts.add(v.value);
                        newValues.add(v);
                    }
                }
                values = newValues;
            }
            if (values.size() > 1) {
                value = values.get(0).value;
                effKey1 = values.get(0).effKey;
            }
            return new ValueWithDistance<V>(
                    key, effKey1, value, values, distance, false
            );
        }

        public static <V> ValueWithDistance<V> ofEmpty(NPlatformSignature key) {
            return new ValueWithDistance<V>(
                    key, key, null, null, 0, true
            );
        }

        public ValueWithDistance(NPlatformSignature key, NPlatformSignature effKey, V value, List<ValueWithDistance<V>> values, int distance, boolean empty) {
            this.key = key;
            this.effKey = effKey;
            this.value = value;
            this.pointers = values;
            this.distance = distance;
            this.empty = empty;
        }

        public V getValueNonAmbiguous() {
            if (pointers != null && pointers.size() > 1) {
                throw new IllegalArgumentException(NMsg.ofC("Not a single value %s : %s", key,
                        pointers.stream().map(x -> x.key).collect(Collectors.toList())
                ).toString());
            }
            if (pointers != null && pointers.size() == 1) {
                return pointers.get(0).getValueNonAmbiguous();
            }
            return value;
        }

        public boolean isPresent() {
            return !empty;
        }
    }

}
