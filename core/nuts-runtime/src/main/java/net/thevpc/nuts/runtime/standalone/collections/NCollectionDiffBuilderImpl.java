package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.*;
import net.thevpc.nuts.util.NDiffMode;
import net.thevpc.nuts.util.NEqualizer;

import java.util.*;
import java.util.function.Function;

/**
 * NCollectionDiffBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCollectionDiffBuilderImpl<T, K> implements NCollectionDiffBuilder<T, K> {
    private Collection<T> oldItems;
    private Collection<T> newItems;
    private Function<T, K> idResolver;
    private NEqualizer<T> equalizer;

  /**
   * N collection diff builder.
   */
    public NCollectionDiffBuilderImpl() {
    }

    /**
     * Old items.
     *
     * @return old items result
     */
    @Override
    public Collection<T> oldItems() {
        return oldItems;
    }

    /**
     * Old items.
     *
     * @param oldItems old items
     * @return old items result
     */
    @Override
    public NCollectionDiffBuilder<T, K> oldItems(Collection<T> oldItems) {
        this.oldItems = oldItems;
        return this;
    }

    /**
     * New items.
     *
     * @return new items result
     */
    @Override
    public Collection<T> newItems() {
        return newItems;
    }

    /**
     * New items.
     *
     * @param newItems new items
     * @return new items result
     */
    @Override
    public NCollectionDiffBuilder<T, K> newItems(Collection<T> newItems) {
        this.newItems = newItems;
        return this;
    }

    /**
     * Id resolver.
     *
     * @return id resolver result
     */
    @Override
    public Function<T, K> idResolver() {
        return idResolver;
    }

    /**
     * Id resolver.
     *
     * @param idResolver id resolver
     * @return id resolver result
     */
    @Override
    public NCollectionDiffBuilder<T, K> idResolver(Function<T, K> idResolver) {
        this.idResolver = idResolver;
        return this;
    }

    /**
     * Equalizer.
     *
     * @return equalizer result
     */
    @Override
    public NEqualizer<T> equalizer() {
        return equalizer;
    }

    /**
     * Equalizer.
     *
     * @param equalize equalize
     * @return equalizer result
     */
    @Override
    public NCollectionDiffBuilder<T, K> equalizer(NEqualizer<T> equalize) {
        this.equalizer = equalize;
        return this;
    }

    /**
     * Diff.
     *
     * @return diff result
     */
    @Override
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
        NCollectionDiffImpl<T> d = new NCollectionDiffImpl<>(oldItems, newItems);
        for (Map.Entry<K, List<NValueAndPos<T>>> f : oldMap.entrySet()) {
            List<NValueAndPos<T>> values = f.getValue();
            for (Iterator<NValueAndPos<T>> iterator = values.iterator(); iterator.hasNext(); ) {
                NValueAndPos<T> oldValue = iterator.next();
                iterator.remove();
                List<NValueAndPos<T>> inB = newMap.get(f.getKey());
                if (inB != null && inB.size() > 0) {
                    NValueAndPos<T> n = inB.remove(0);
                    if (equalizer.equals(oldValue.value, n.value)) {
                        d.register(new NCollectionDiffChangeImpl<>(NDiffMode.UNCHANGED, oldValue, n));
                    } else {
                        d.register(new NCollectionDiffChangeImpl<>(NDiffMode.CHANGED, oldValue, n));
                    }
                } else {
                    d.register(new NCollectionDiffChangeImpl<>(NDiffMode.REMOVED, oldValue, new NValueAndPos<>(null, -1)));
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
                        d.register(new NCollectionDiffChangeImpl<>(NDiffMode.UNCHANGED, newValue, n));
                    } else {
                        d.register(new NCollectionDiffChangeImpl<>(NDiffMode.CHANGED, newValue, n));
                    }
                } else {
                    d.register(new NCollectionDiffChangeImpl<>(NDiffMode.ADDED, new NValueAndPos<>(null, -1), newValue));
                }
            }
        }
        d.sort();
        return d;
    }
}
