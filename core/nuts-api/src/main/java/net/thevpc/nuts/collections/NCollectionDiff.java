package net.thevpc.nuts.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface NCollectionDiff<T> extends Iterable<NCollectionDiffChange<T>> {
    /**
     * Diff list.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff list result
     */
    static <T> NCollectionDiff<T> diffList(List<T> oldItems, List<T> newItems) {
        /**
         * Diff list.
         *
         * @param oldItems old items
         * @param newItems new items
         * @param x x
         * @return diff list result
         */
        return diffList(oldItems, newItems, x -> x);
    }

    /**
     * Diff map entries.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map entries result
     */
    static <K, V> NCollectionDiff<Map.Entry<K, V>> diffMapEntries(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff list.
         *
         * @param oldItems.entrySet() old items.entry set()
         * @param newItems.entrySet() new items.entry set()
         * @param Map.Entry::getKey map. entry::get key
         * @return diff list result
         */
        return diffList(oldItems.entrySet(), newItems.entrySet(), Map.Entry::getKey);
    }

    /**
     * Diff map values.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map values result
     */
    static <K, V> NCollectionDiff<V> diffMapValues(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff map entries.
         *
         * @param oldItems old items
         * @param newItems).map(Map.Entry::getValue new items).map( map. entry::get value
         * @return diff map entries result
         */
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getValue);
    }

    /**
     * Diff map keys.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map keys result
     */
    static <K, V> NCollectionDiff<K> diffMapKeys(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff map entries.
         *
         * @param oldItems old items
         * @param newItems).map(Map.Entry::getKey new items).map( map. entry::get key
         * @return diff map entries result
         */
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getKey);
    }

    /**
     * Diff list.
     *
     * @param oldItems old items
     * @param newItems new items
     * @param id       id
     * @return diff list result
     */
    static <T, K> NCollectionDiff<T> diffList(Collection<T> oldItems, Collection<T> newItems, Function<T, K> id) {
        return NCollectionDiffBuilder.<T, K>of().oldItems(oldItems).newItems(newItems).idResolver(id).diff();
    }

    /**
     * Creates a new instance of of.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return of result
     */
    static <T, K> NCollectionDiffBuilder<T, K> of(Collection<T> oldItems, Collection<T> newItems) {
        return NCollectionDiffBuilder.<T, K>of().oldItems(oldItems).newItems(newItems);
    }

    Collection<T> oldItems();

    Collection<T> newItems();

    boolean anyAdd();

    @Override
    Iterator<NCollectionDiffChange<T>> iterator();

    boolean anyRemove();

    boolean anyChange();

    List<T> added();

    List<T> removed();

    List<NCollectionDiffChange<T>> changed();

    List<NCollectionDiffChange<T>> unchanged();

    <H> NCollectionDiff<H> map(Function<T, H> f);

    void apply(NCollectionDiffApplier<T> a);
}
