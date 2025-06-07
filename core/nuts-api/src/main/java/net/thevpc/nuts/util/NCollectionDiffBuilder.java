package net.thevpc.nuts.util;

import java.util.*;
import java.util.function.Function;

public class NCollectionDiffBuilder<T, K> {
    private Collection<T> oldItems;
    private Collection<T> newItems;
    private Function<T, K> idResolver;
    private NEqualizer<T> equalizer;

    NCollectionDiffBuilder() {
    }

    public Collection<T> getOldItems() {
        return oldItems;
    }

    public NCollectionDiffBuilder<T, K> setOldItems(Collection<T> oldItems) {
        this.oldItems = oldItems;
        return this;
    }

    public Collection<T> getNewItems() {
        return newItems;
    }

    public NCollectionDiffBuilder<T, K> setNewItems(Collection<T> newItems) {
        this.newItems = newItems;
        return this;
    }

    public Function<T, K> getIdResolver() {
        return idResolver;
    }

    public NCollectionDiffBuilder<T, K> setIdResolver(Function<T, K> idResolver) {
        this.idResolver = idResolver;
        return this;
    }

    public NEqualizer<T> getEqualizer() {
        return equalizer;
    }

    public NCollectionDiffBuilder<T, K> setEqualizer(NEqualizer<T> equalize) {
        this.equalizer = equalize;
        return this;
    }

    public NCollectionDiff<T> diff() {
        Function<T, K> id = this.idResolver;
        if (id == null) {
            id = x -> (K) x;
        }
        NEqualizer<T> equalizer = this.equalizer;
        if (equalizer == null) {
            equalizer = (a, b) -> Objects.equals(a, b);
        }

        Map<K, List<NValueAndPos<T>>> oldMap = new LinkedHashMap<>();
        Map<K, List<NValueAndPos<T>>> newMap = new LinkedHashMap<>();
        if (oldItems != null) {
            int pos = 0;
            for (T i : oldItems) {
                K k = (i == null) ? null : id.apply(i);
                List<NValueAndPos<T>> e = oldMap.computeIfAbsent(k, x -> new ArrayList<>());
                e.add(new NValueAndPos<>(i, pos));
                pos++;
            }
        }
        if (newItems != null) {
            int pos = 0;
            for (T i : newItems) {
                K k = (i == null) ? null : id.apply(i);
                List<NValueAndPos<T>> e = newMap.computeIfAbsent(k, x -> new ArrayList<>());
                e.add(new NValueAndPos<>(i, pos));
                pos++;
            }
        }
        NCollectionDiff<T> d = new NCollectionDiff<>(oldItems, newItems);
        for (Map.Entry<K, List<NValueAndPos<T>>> f : oldMap.entrySet()) {
            List<NValueAndPos<T>> values = f.getValue();
            for (Iterator<NValueAndPos<T>> iterator = values.iterator(); iterator.hasNext(); ) {
                NValueAndPos<T> oldValue = iterator.next();
                iterator.remove();
                List<NValueAndPos<T>> inB = newMap.get(f.getKey());
                if (inB != null && inB.size() > 0) {
                    NValueAndPos<T> n = inB.remove(0);
                    if (equalizer.equals(oldValue.value, n.value)) {
                        d.register(new NCollectionDiffChange<>(NDiffMode.UNCHANGED, oldValue, n));
                    } else {
                        d.register(new NCollectionDiffChange<>(NDiffMode.CHANGED, oldValue, n));
                    }
                } else {
                    d.register(new NCollectionDiffChange<>(NDiffMode.REMOVED, oldValue, new NValueAndPos<>(null, -1)));
                }
            }
        }
        for (Map.Entry<K, List<NValueAndPos<T>>> f : newMap.entrySet()) {
            List<NValueAndPos<T>> values = f.getValue();
            for (Iterator<NValueAndPos<T>> iterator = values.iterator(); iterator.hasNext(); ) {
                NValueAndPos<T> newValue = iterator.next();
                iterator.remove();
                List<NValueAndPos<T>> inA = oldMap.get(f.getKey());
                if (inA != null && inA.size() > 0) {
                    NValueAndPos<T> n = inA.remove(0);
                    if (equalizer.equals(newValue.value, n.value)) {
                        d.register(new NCollectionDiffChange<>(NDiffMode.UNCHANGED, newValue, n));
                    } else {
                        d.register(new NCollectionDiffChange<>(NDiffMode.CHANGED, newValue, n));
                    }
                } else {
                    d.register(new NCollectionDiffChange<>(NDiffMode.ADDED, new NValueAndPos<>(null, -1), newValue));
                }
            }
        }
        d.sort();
        return d;
    }
}
