package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NSignature;
import net.thevpc.nuts.reflect.NSignatureDomain;
import net.thevpc.nuts.reflect.NSignatureMap;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NOptionalMap;

import java.util.*;
import java.util.stream.Collectors;

public class NSignatureMapImpl<S extends NSignature<T, ?>, T, V> implements NSignatureMap<S, T, V> {
    private final Map<Integer, NSigMapBySize<V>> map = new HashMap<>();
    private final NSignatureDomain<T> domain;

    public NSignatureMapImpl(NSignatureDomain<T> domain) {
        this.domain = domain;
    }

    @Override
    public void putMulti(S sig, V value, S... sigs) {
        NAssert.requireNamedNonNull(sig);
        synchronized (map) {
            int usize = sig.size();
            NSigMapBySize<V> m = map.get(usize);
            if (m == null) {
                m = new NSigMapBySize<>(usize);
                map.put(usize, m);
            }
            m.put(sig, value);
            if (sigs != null) {
                for (S nSig : sigs) {
                    if (nSig != null) {
                        m.put(nSig, value);
                    }
                }
            }
        }
    }

    @Override
    public void put(S sig, V value) {
        NAssert.requireNamedNonNull(sig);
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

    @Override
    public NOptional<V> get(S sig) {
        NAssert.requireNamedNonNull(sig);
        synchronized (map) {
            NSigMapBySize<V> m = map.get(sig.size());
            if (m == null) {
                return NOptional.ofNamedEmpty(NMsg.ofC("%s", sig));
            }
            return m.get(sig);
        }
    }

    @Override
    public void remove(S sig) {
        NAssert.requireNamedNonNull(sig);
        synchronized (map) {
            NSigMapBySize<V> m = map.get(sig.size());
            if (m == null) {
                return;
            }
            m.remove(sig);
        }
    }

    @Override
    public Map<S, V> toMap() {
        Map<S, V> all = new HashMap<>();
        for (Map.Entry<Integer, NSigMapBySize<V>> e : map.entrySet()) {
            all.putAll(e.getValue().map.toMap());
        }
        return all;
    }

    @Override
    public int size() {
        int s = 0;
        for (Map.Entry<Integer, NSigMapBySize<V>> e : map.entrySet()) {
            s += e.getValue().map.size();
        }
        return s;
    }

    private class NSigMapBySize<V> {
        private final NOptionalMap<S, V> map = new NOptionalMap<>();
        private final int count;
        private boolean invalidCache;
        private final Map<S, ValueWithDistance<V>> cache = new HashMap<>();
        private final Set<S> findInProgress = new HashSet<>();

        public NSigMapBySize(int count) {
            this.count = count;
        }

        public NOptional<V> get(S sig) {
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
                    cache.put(sig, ofSimpleValueWithDistance(sig, sig, 0, v.get()));
                    return v;
                }
            }
            ValueWithDistance<V> vd = find(sig, 0);
            if (vd == null) {
                cache.put(sig, vd = ofEmptyValueWithDistance(sig));
            }
            return NOptional.ofNullable(vd.getValueNonAmbiguous());
        }

        public ValueWithDistance<V> find(S sig, int distance) {
            if (findInProgress.contains(sig)) {
                return null;
            }
            try {
                findInProgress.add(sig);
                NOptional<V> v = map.get(sig);
                if (v.isPresent()) {
                    return ofSimpleValueWithDistance(sig, sig, distance, v.get());
                }
                ValueWithDistanceBestResolver<V> bestResolver = new ValueWithDistanceBestResolver<>();
                for (int i = 0; i < sig.size(); i++) {
                    T t = sig.getType(i);
                    findClassAtPos(sig, i, t, bestResolver, distance);
                }
                ValueWithDistance<V> r = ofValueWithDistancePointers(sig, distance, bestResolver.best);
                return r;
            } finally {
                findInProgress.remove(sig);
            }
        }

        private void findClassAtPos(S sig, int i, T c, ValueWithDistanceBestResolver<V> bestResolver, int distance) {
            if (domain.isPrimitive(c)) {
                T bt = domain.toBoxedType(c);
                ValueWithDistance<V> vd = find((S) sig.set(bt, i), distance + 1);
                bestResolver.add(vd);
            } else {
                T bt = domain.toPrimitiveType(c);
                if (bt != null) {
                    ValueWithDistance<V> vd = find((S) sig.set(bt, i), distance + 1);
                    bestResolver.add(vd);
                }
            }
            T sc = domain.getSuperType(c);
            if (sc != null) {
                ValueWithDistance<V> vd = find((S) sig.set(sc, i), distance + 2);
                bestResolver.add(vd);
            }
            T[] superInterfaces = domain.getInterfaces(c);
            for (T si : superInterfaces) {
                ValueWithDistance<V> vd = find((S) sig.set(si, i), distance + 2);
                bestResolver.add(vd);
            }
        }

        public NOptional<V> remove(S uplet) {
            NOptional<V> r = map.remove(uplet);
            if (r.isPresent()) {
                invalidCache = true;
            }
            return r;
        }

        public void put(S uplet, V value) {
            NOptional<V> o = map.put(uplet, value);
            if (!o.isPresent() || !Objects.equals(o.orNull(), value)) {
                invalidCache = true;
            }
        }

    }

    private class ValueWithDistanceBestResolver<V> {
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
                        best.addAll(vd.pointers.stream().map(x -> ofSimpleValueWithDistance(vd.key, vd.effKey, vd.distance, x.value)).collect(Collectors.toList()));
                    }
                } else if (bestDistance == vd.distance) {
                    if (vd.pointers == null) {
                        best.add(vd);
                    } else {
                        best.addAll(vd.pointers.stream().map(x -> ofSimpleValueWithDistance(vd.key, vd.effKey, vd.distance, x.value)).collect(Collectors.toList()));
                    }
                }
            }
        }
    }

    public <V> ValueWithDistance<V> ofSimpleValueWithDistance(S key, S effKey, int distance, V value) {
        return new ValueWithDistance<V>(
                key, effKey, value, null, distance, false
        );
    }

    public <V> ValueWithDistance<V> ofEmptyValueWithDistance(S key) {
        return new ValueWithDistance<V>(
                key, key, null, null, 0, true
        );
    }

    public <V> ValueWithDistance<V> ofValueWithDistancePointers(S key, int distance, List<ValueWithDistance<V>> values) {
        if (values.size() == 0) {
            return ofEmptyValueWithDistance(key);
        }
        V value = null;
        S effKey1 = key;
        if (values.size() == 1) {
            value = values.get(0).value;
            effKey1 = values.get(0).effKey;
        }
        //remove duplicates if the very same impl (aka effective key)
        if (values.size() > 1) {
            LinkedHashMap<S, ValueWithDistance<V>> valuesMap = new LinkedHashMap<>();
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

    private class ValueWithDistance<V> {
        S key;
        S effKey;
        V value;
        List<ValueWithDistance<V>> pointers;
        int distance;
        boolean empty;


        public ValueWithDistance(S key, S effKey, V value, List<ValueWithDistance<V>> values, int distance, boolean empty) {
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
